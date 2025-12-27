package use_case.upload_paper_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.AnswerPaper;
import entities.ExamPaper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import use_case.Constants;
import use_case.dto.UploadPaperAnswerInputData;
import use_case.dto.UploadPaperAnswerOutputData;
import use_case.util.ApiUtil;
import use_case.util.FileUtil;
import use_case.util.ThreadUtil;

public class UploadPaperAnswerUseCase implements UploadPaperAnswerInputBoundary{

    private final UploadPaperAnswerOutputBoundary uploadPaperAnswerOutputBoundary;
    private final UploadPaperAnswerDataAccessInterface dao;

    // --- 1. 定义处理类型枚举 ---
    private enum DocType {
        EXAM("EXAM_", "exam"),
        ANSWER("ANSWER_", "answer");

        final String prefix;
        final String apiType;

        DocType(String prefix, String apiType) {
            this.prefix = prefix;
            this.apiType = apiType;
        }
    }

    public UploadPaperAnswerUseCase(UploadPaperAnswerOutputBoundary outputBoundary,
                                    UploadPaperAnswerDataAccessInterface dao) {
        this.uploadPaperAnswerOutputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(UploadPaperAnswerInputData inputData) {
        try {
            // 1. 并行处理文档（内部已包含 OCR + LLM 流程）
            JSONObject examObj = processDocumentToJSONObject(inputData.getExamFilePath(), DocType.EXAM);
            JSONObject answerObj = processDocumentToJSONObject(inputData.getAnswerFilePath(), DocType.ANSWER);

            // 2. 双向关联 ID
            String examId = examObj.getString("id");
            String answerId = answerObj.getString("id");
            examObj.put("answerId", answerId);
            answerObj.put("examPaperId", examId);

            // 3. 存储
            ExamPaper examPaper = ExamPaper.jsonToExamPaper(examObj.toJSONString());
            AnswerPaper answerPaper = AnswerPaper.jsonToAnswerPaper(answerObj.toJSONString());
            dao.storeExamAnswer(examPaper, answerPaper);

            uploadPaperAnswerOutputBoundary.prepareSuccessView(new UploadPaperAnswerOutputData(
                    examPaper.getId(), answerPaper.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            uploadPaperAnswerOutputBoundary.prepareFailView(new UploadPaperAnswerOutputData("", ""));
        }
    }

    private JSONObject processDocumentToJSONObject(String filePath, DocType docType) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) throw new FileNotFoundException("文件不存在: " + filePath);

        String docId = docType.prefix + (docType == DocType.EXAM ?
                UUID.randomUUID().toString().substring(0, 8) : System.currentTimeMillis());

        String extension = FileUtil.getFileExtension(filePath).toLowerCase();

        // 1. 定义存储 OCR 文本结果的任务列表 (不再直接存储 API 返回的 JSONObject)
        List<CompletableFuture<String>> ocrFutures = new ArrayList<>();

        if ("pdf".equals(extension)) {
            int pageCount;
            try (PDDocument metaDoc = PDDocument.load(file)) {
                pageCount = metaDoc.getNumberOfPages();
            }

            for (int i = 0; i < pageCount; i++) {
                final int pageIdx = i;
                ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                    try (PDDocument document = PDDocument.load(file)) {
                        PDFRenderer renderer = new PDFRenderer(document);
                        BufferedImage image = renderer.renderImageWithDPI(pageIdx, 144);
                        try {
                            if (image == null) throw new RuntimeException("渲染为空");
                            // 仅执行 OCR
                            return ocrProcess(image);
                        } finally {
                            if (image != null) image.flush();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("第 " + pageIdx + " 页 OCR 失败", e);
                    }
                }, ThreadUtil.getExecutor()));
            }
        } else if ("png".equals(extension) || "jpg".equals(extension)) {
            BufferedImage image = ImageIO.read(file);
            ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return ocrProcess(image);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, ThreadUtil.getExecutor()));
        }

        // 2. 等待所有页面的 OCR 完成
        CompletableFuture.allOf(ocrFutures.toArray(new CompletableFuture[0])).join();

        // 3. 汇总所有 OCR 结果
        StringBuilder fullOcrText = new StringBuilder();
        for (int i = 0; i < ocrFutures.size(); i++) {
            fullOcrText.append("--- 第 ").append(i + 1).append(" 页内容 ---\n");
            fullOcrText.append(ocrFutures.get(i).get()).append("\n\n");
        }

        // 4. 构造统一的 Prompt 并调用一次 API
        String basePrompt = "answer".equals(docType.apiType) ? Constants.ANSWER_PROMPT : Constants.EXAM_PROMPT;
        String combinedPrompt = basePrompt + "\n\n以下是整份文档的 OCR 识别结果，请统一处理：\n" + fullOcrText.toString();

        JSONObject apiResponse = callQwenVlApi(combinedPrompt);

        // 5. 组装最终结果
        // 因为现在只有一次 API 调用，我们可以简化处理，或者兼容原有的 assembleResults 逻辑
        return finalizeResult(docId, apiResponse, docType);
    }

    private JSONObject callQwenVlApi(String combinedPrompt) throws Exception {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = getHttpPost(combinedPrompt);
            String responseContent = httpClient.execute(httpPost, response ->
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            return parseQwenResponse(responseContent);
        }
    }

    private HttpPost getHttpPost(String prompt) {
        HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
        httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen3-vl-flash");

        JSONObject message = new JSONObject();
        message.put("role", "user");
        JSONArray content = new JSONArray();
        content.add(new JSONObject().fluentPut("text", prompt));
        // content.add(new JSONObject().fluentPut("image", "data:image/png;base64," + base64Image));

        message.put("content", content);
        requestBody.put("input", new JSONObject().fluentPut("messages", Collections.singletonList(message)));

        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON));
        return httpPost;
    }

    // --- 以下为未变动的工具函数 ---

    private JSONObject finalizeResult(String id, JSONObject apiData, DocType docType) {
        JSONObject root = new JSONObject(new LinkedHashMap<>());
        root.put("id", id);
        root.put("subject", apiData.getString("subject") != null ? apiData.getString("subject") : "");
        root.put("questions", apiData.getJSONObject("questions"));
        if (docType == DocType.ANSWER) {
            root.put("answers", apiData.getJSONObject("answers"));
        }
        return root;
    }

    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);
        String contentText = responseObj.getJSONObject("output").getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getJSONArray("content").getJSONObject(0).getString("text");
        String cleanJson = contentText.replaceAll("```json", "").replaceAll("```", "").trim();
        return JSON.parseObject(cleanJson);
    }

    private String ocrProcess(BufferedImage image) throws Exception {
        return ApiUtil.getLLMResponseFromImage(image, Constants.OCR_PROMPT);
//        return Constants.TEST_OCR_RESPONSE;
    }
}

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
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
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
        List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

        if ("pdf".equals(extension)) {
            int pageCount;
            try (PDDocument metaDoc = Loader.loadPDF(file)) {
                pageCount = metaDoc.getNumberOfPages();
            }

            for (int i = 0; i < pageCount; i++) {
                final int pageIdx = i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try (RandomAccessReadBufferedFile raFile = new RandomAccessReadBufferedFile(file);
                         PDDocument document = Loader.loadPDF(raFile)) {

                        PDFRenderer renderer = new PDFRenderer(document);
                        BufferedImage image = renderer.renderImageWithDPI(pageIdx, 144);
                        try {
                            if (image == null) throw new RuntimeException("渲染为空");

                            // --- 核心改动：先执行 OCR ---
                            String ocrResult = ocrProcess(image);

                            // 将 OCR 结果作为上下文传给大模型
                            return callQwenVlApi(docId, encodeImageToBase64(image), ocrResult, docType.apiType);
                        } finally {
                            if (image != null) image.flush();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("第 " + pageIdx + " 页处理失败", e);
                    }
                }, ThreadUtil.getExecutor()));
            }
        } else if ("png".equals(extension) || "jpg".equals(extension)) {
            BufferedImage image = ImageIO.read(file);
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    String ocrResult = ocrProcess(image);
                    return callQwenVlApi(docId, encodeImageToBase64(image), ocrResult, docType.apiType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, ThreadUtil.getExecutor()));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return assembleResults(docId, futures, docType);
    }

    private JSONObject callQwenVlApi(String id, String base64, String ocrContent, String type) throws Exception {
        // 构造 Prompt，告诉模型结合 OCR 文本进行结构化
        String basePrompt = "answer".equals(type) ? Constants.ANSWER_PROMPT : Constants.EXAM_PROMPT;
        String combinedPrompt = basePrompt + "\n\n以下是该图片的 OCR 识别结果，供参考：\n" + ocrContent;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = getHttpPost(base64, combinedPrompt);
            String responseContent = httpClient.execute(httpPost, response ->
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            return parseQwenResponse(responseContent);
        }
    }

    private HttpPost getHttpPost(String base64Image, String prompt) {
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

    private JSONObject assembleResults(String id, List<CompletableFuture<JSONObject>> futures, DocType docType) throws Exception {
        JSONObject root = new JSONObject(new LinkedHashMap<>());
        root.put("id", id);
        root.put("subject", "");
        JSONObject allQuestions = new JSONObject(new LinkedHashMap<>());
        JSONObject allAnswers = new JSONObject(new LinkedHashMap<>());

        for (CompletableFuture<JSONObject> future : futures) {
            JSONObject pageData = future.get();
            if (root.getString("subject").isEmpty() && pageData.containsKey("subject")) {
                root.put("subject", pageData.getString("subject"));
            }
            if (pageData.containsKey("questions")) allQuestions.putAll(pageData.getJSONObject("questions"));
            if (docType == DocType.ANSWER && pageData.containsKey("answers")) allAnswers.putAll(pageData.getJSONObject("answers"));
        }
        root.put("questions", allQuestions);
        if (docType == DocType.ANSWER) root.put("answers", allAnswers);
        return root;
    }

    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);
        String contentText = responseObj.getJSONObject("output").getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getJSONArray("content").getJSONObject(0).getString("text");
        String cleanJson = contentText.replaceAll("```json", "").replaceAll("```", "").trim();
        return JSON.parseObject(cleanJson);
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String ocrProcess(BufferedImage image) throws Exception {
//        return getLLMResponseFromImage(image, Constants.OCR_PROMPT);
        return Constants.TEST_OCR_RESPONSE;
    }

    public String getLLMResponseFromImage(BufferedImage image, String prompt) {
        ObjectMapper mapper = new ObjectMapper();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // 1. 将 BufferedImage 转换为 Base64 字符串
            String base64Image = encodeImageToBase64(image);

            // 2. 构造请求体 (OpenAI Vision 格式)
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("model", Constants.OCR_MODEL_NAME);

            ArrayNode messagesArray = rootNode.putArray("messages");
            ObjectNode userMessage = messagesArray.addObject();
            userMessage.put("role", "user");

            // 多模态 content 是一个数组
            ArrayNode contentArray = userMessage.putArray("content");

            // 文本部分
            ObjectNode textContent = contentArray.addObject();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            // 图片部分
            ObjectNode imageContent = contentArray.addObject();
            imageContent.put("type", "image_url");
            ObjectNode imageUrl = imageContent.putObject("image_url");
            // 格式必须为 data:image/png;base64,{base64_data}
            imageUrl.put("url", "data:image/png;base64," + base64Image);

            // 3. 发送请求
            HttpPost httpPost = new HttpPost(Constants.OCR_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + Constants.OCR_API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(
                    mapper.writeValueAsString(rootNode),
                    ContentType.APPLICATION_JSON
            );
            httpPost.setEntity(entity);

            return httpClient.execute(httpPost, response -> {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return mapper.readTree(responseString)
                            .path("choices").get(0)
                            .path("message").path("content").asText();
                } else {
                    return "API Error: " + response.getStatusLine().getStatusCode() + " - " + responseString;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }
}

package use_case.upload_student_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.StudentPaper;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import use_case.Constants;
import use_case.dto.UploadStudentAnswerInputData;
import use_case.dto.UploadStudentAnswerOutputData;
import use_case.util.FileUtil;
import use_case.util.ThreadUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UploadStudentAnswerUseCase implements UploadStudentAnswerInputBoundary{

    private final UploadStudentAnswerDataAccessInterface dao;
    private final UploadStudentAnswerOutputBoundary outputBoundary;

    public UploadStudentAnswerUseCase(UploadStudentAnswerDataAccessInterface dao,
                                      UploadStudentAnswerOutputBoundary outputBoundary) {
        this.dao = dao;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(UploadStudentAnswerInputData inputData) {
        List<String> paths = inputData.getPaths();
        String examPaperId = inputData.getExamPaperId();
        List<StudentPaper> papers = new ArrayList<>();

        try {
            for (String path : paths) {
                // 1. 为每份文件生成学生试卷 ID
                String studentPaperId = "STU_" + UUID.randomUUID().toString().substring(0, 8);

                // 2. 调用核心处理逻辑（支持多页并行 OCR + LLM）
                JSONObject resultJson = processStudentDocument(path, studentPaperId, examPaperId);

                // 3. 转换为 POJO
                papers.add(StudentPaper.jsonToStudentPaper(resultJson.toJSONString()));
            }

            // 4. 持久化存储
            dao.saveStudentPapers(papers);

            // 5. 返回成功视图
            outputBoundary.prepareSuccessView(new UploadStudentAnswerOutputData());

        } catch (Exception e) {
            e.printStackTrace();
            outputBoundary.prepareFailView(new UploadStudentAnswerOutputData());
        }
    }

    /**
     * 处理单个文档（PDF 或 图片）
     */
    private JSONObject processStudentDocument(String path, String studentPaperId, String examPaperId) throws Exception {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("文件不存在: " + path);

        String extension = FileUtil.getFileExtension(path).toLowerCase();
        List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

        if ("pdf".equals(extension)) {
            // 获取总页数
            try (PDDocument metaDoc = Loader.loadPDF(file)) {
                int pageCount = metaDoc.getNumberOfPages();
                for (int i = 0; i < pageCount; i++) {
                    final int pageIdx = i;
                    // 并行处理每一页
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try (RandomAccessReadBufferedFile raFile = new RandomAccessReadBufferedFile(file);
                             PDDocument document = Loader.loadPDF(raFile)) {

                            PDFRenderer renderer = new PDFRenderer(document);
                            BufferedImage image = renderer.renderImageWithDPI(pageIdx, 144);
                            return processSinglePage(image, studentPaperId);
                        } catch (Exception e) {
                            throw new RuntimeException("处理第 " + pageIdx + " 页失败", e);
                        }
                    }, ThreadUtil.getExecutor()));
                }
            }
        } else if ("png".equals(extension) || "jpg".equals(extension)) {
            BufferedImage image = ImageIO.read(file);
            if (image == null) throw new IOException("无法解码图片");
            futures.add(CompletableFuture.supplyAsync(() -> processSinglePage(image, studentPaperId), ThreadUtil.getExecutor()));
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 等待所有页面任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 合并多页 JSON 结果
        JSONObject mergedJson = mergeStudentResults(futures);
        mergedJson.put("id", studentPaperId);
        mergedJson.put("examPaperId", examPaperId);

        return mergedJson;
    }

    /**
     * 单页处理：OCR -> Qwen-VL
     */
    private JSONObject processSinglePage(BufferedImage image, String id) {
        try {
            // 1. 执行 OCR 识别文字和位置
            String ocrResult = ocrProcess(image);

            // 2. 图片转 Base64 供 Qwen-VL 使用
            String base64 = encodeImageToBase64(image);

            // 3. 调用 Qwen-VL API (传入 Prompt + 图片 + OCR 辅助信息)
            JSONObject llmResponse = callQwenVlApi(id, base64, ocrResult);

            // 4. 将 OCR 原始数据存入，用于后续溯源
            llmResponse.put("coordContent", ocrResult);

            return llmResponse;
        } catch (Exception e) {
            throw new RuntimeException("单页识别核心链路失败", e);
        } finally {
            if (image != null) image.flush(); // 释放内存
        }
    }

    /**
     * 调用 Qwen3-VL (Flash/Max) API
     */
    private JSONObject callQwenVlApi(String id, String base64, String ocrContent) throws Exception {
        // 增强 Prompt：结合视觉和 OCR 文本
        String combinedPrompt = Constants.STUDENT_PROMPT + "\n【参考 OCR 识别结果】\n" + ocrContent;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            // 构建符合 DashScope 规范的请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen3-vl-flash");

            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray content = new JSONArray();
            content.add(new JSONObject().fluentPut("text", combinedPrompt));
            content.add(new JSONObject().fluentPut("image", "data:image/png;base64," + base64));

            message.put("content", content);
            requestBody.put("input", new JSONObject().fluentPut("messages", Collections.singletonList(message)));

            String responseContent = httpClient.execute(httpPost, response -> {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("API 调用失败: " + response.getStatusLine().getStatusCode());
                }
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            });

            return parseQwenResponse(responseContent);
        }
    }

    /**
     * 解析 Qwen 返回的 Markdown JSON
     */
    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);
        String text = responseObj.getJSONObject("output")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text");

        String cleanJson = text.replaceAll("```json", "").replaceAll("```", "").trim();
        return JSON.parseObject(cleanJson);
    }

    /**
     * 合并各页数据
     */
    private JSONObject mergeStudentResults(List<CompletableFuture<JSONObject>> futures) throws Exception {
        JSONObject root = new JSONObject(new LinkedHashMap<>());
        JSONObject allAnswers = new JSONObject(new LinkedHashMap<>());
        StringBuilder ocrLog = new StringBuilder();

        for (CompletableFuture<JSONObject> future : futures) {
            JSONObject pageData = future.get();
            if (pageData.containsKey("answers")) {
                allAnswers.putAll(pageData.getJSONObject("answers"));
            }
            if (pageData.containsKey("coordContent")) {
                ocrLog.append(pageData.getString("coordContent")).append("\n");
            }
        }

        root.put("answers", allAnswers);
        root.put("coordContent", ocrLog.toString());
        return root;
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String ocrProcess(BufferedImage image) throws Exception {
        // 使用你指定的 ocrProcess(BufferedImage) 逻辑
        String base64 = encodeImageToBase64(image);
        // TODO: ... 此处为你的 OCR API 调用逻辑 ...
        return "";
    }
}

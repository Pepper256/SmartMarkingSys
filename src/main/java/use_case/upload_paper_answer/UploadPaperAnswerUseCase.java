package use_case.upload_paper_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import entities.AnswerPaper;
import entities.ExamPaper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
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
import use_case.util.ThreadUtil;

public class UploadPaperAnswerUseCase implements UploadPaperAnswerInputBoundary{

    private final UploadPaperAnswerOutputBoundary uploadPaperAnswerOutputBoundary;
    private final UploadPaperAnswerDataAccessInterface dao;

    public UploadPaperAnswerUseCase(UploadPaperAnswerOutputBoundary outputBoundary,
                                    UploadPaperAnswerDataAccessInterface dao) {
        this.uploadPaperAnswerOutputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(UploadPaperAnswerInputData inputData) {
        String examFilePath = inputData.getExamFilePath();
        String answerFilePath = inputData.getAnswerFilePath();

        try {
            String examJson = processExamPdf(examFilePath);
            String answerJson = processAnswerPdf(answerFilePath);

            JSONObject temp = JSON.parseObject(examJson);
            temp.put("answerId", JSON.parseObject(answerJson).getString("id"));
            examJson = temp.toJSONString();

            dao.storeExamAnswer(ExamPaper.jsonToExamPaper(examJson), AnswerPaper.jsonToAnswerPaper(answerJson));

            uploadPaperAnswerOutputBoundary.prepareSuccessView(new UploadPaperAnswerOutputData(
                    ExamPaper.jsonToExamPaper(examJson).getId(),
                    AnswerPaper.jsonToAnswerPaper(answerJson).getId()));
        }
        catch (Exception e) {
            uploadPaperAnswerOutputBoundary.prepareFailView(new UploadPaperAnswerOutputData(
                    "",
                    ""
            ));
        }
    }

    private String processExamPdf(String pdfPath) throws Exception {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new FileNotFoundException("PDF文件不存在: " + pdfPath);
        }

        String examId = "EXAM_" + UUID.randomUUID().toString().substring(0, 8);

        if (getFileExtension(pdfPath).equals("pdf"))
        {// 1. 第一次加载仅为了获取总页数
            int pageCount;
            try (PDDocument metaDoc = Loader.loadPDF(file)) {
                pageCount = metaDoc.getNumberOfPages();
            }

            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

            // 2. 循环提交任务
            for (int i = 0; i < pageCount; i++) {
                final int pageIndex = i;
                CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(() -> {
                    // 【核心修复】：为每个子线程创建独立的随机访问文件句柄
                    // 确保线程安全，避免 PDFBox 底层 I/O 冲突导致的 NPE
                    try (RandomAccessReadBufferedFile raFile = new RandomAccessReadBufferedFile(file);
                         PDDocument document = Loader.loadPDF(raFile)) {

                        PDFRenderer renderer = new PDFRenderer(document);

                        // 渲染指定页面
                        BufferedImage image = renderer.renderImageWithDPI(pageIndex, 144);

                        try {
                            if (image == null) {
                                throw new RuntimeException("第 " + pageIndex + " 页渲染结果为空");
                            }
                            String base64 = encodeImageToBase64(image);

                            // 调用 API
                            return callQwenVlApi(examId, base64, pageIndex, "exam");
//                        return new JSONObject();
                        } finally {
                            // 【内存修复】：显式刷新图片资源，释放堆外内存
                            if (image != null) {
                                image.flush();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("第 " + pageIndex + " 页处理异常: " + e.getMessage());
                        throw new RuntimeException("第 " + pageIndex + " 页处理失败", e);
                    }
                }, ThreadUtil.getExecutor());

                futures.add(future);
            }

            try {
                // 3. 等待所有页面处理完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // 4. 合并结果
                return mergeResults(examId, futures);
            } catch (Exception e) {
                // 【健壮性】：异常时清理未完成的任务，防止内存持续泄露
                for (CompletableFuture<JSONObject> f : futures) {
                    if (!f.isDone()) {
                        f.cancel(true);
                    }
                }
                throw new Exception("Exam PDF 处理流程失败", e);
            }
        }
        else if(getFileExtension(pdfPath).equals("png")) {
            // 1. 基础校验
            if (!file.exists()) {
                throw new IOException("文件不存在或为空");
            }

            // 2. 使用 ImageIO 读取文件
            // 如果文件不是图片或格式不支持，read() 会返回 null
            BufferedImage image = ImageIO.read(file);

            // 3. 确保结果不为 null
            if (image == null) {
                throw new IOException("无法解码文件：该文件可能损坏或不是支持的图片格式");
            }

            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

            CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return callQwenVlApi(examId, encodeImageToBase64(image), 0, "exam");
                }
                catch (Exception e) {
                    throw new RuntimeException("图片处理失败");
                }
            }, ThreadUtil.getExecutor());

            futures.add(future);

            return mergeResults(examId, futures);
        }
        else {
            throw new IllegalArgumentException("文件类型不支持");
        }
    }

    private JSONObject callQwenVlApi(String id, String base64, int pageIdx, String type) throws Exception {
        String prompt = null;
        if(type.equals("answer")) {
            prompt = Constants.ANSWER_PROMPT;
        }
        else if (type.equals("exam")) {
            prompt = Constants.EXAM_PROMPT;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = getHttpPost(base64, prompt);

            String responseContent = httpClient.execute(httpPost, response -> {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("API 请求失败，状态码: " + response.getStatusLine().getStatusCode() +
                            ", 原因: " + EntityUtils.toString(response.getEntity()));
                }
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            });

            // 4. 解析 API 返回值
            return parseQwenResponse(responseContent);
        }
    }
    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);
        // 获取模型生成的文本内容 (通常在 output.choices[0].message.content[0].text)
        String contentText = responseObj.getJSONObject("output")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text");

        // 由于模型可能返回带有 ```json ``` 的 Markdown 块，需要清洗
        String cleanJson = contentText.replaceAll("```json", "").replaceAll("```", "").trim();
        return JSON.parseObject(cleanJson);
    }

    private HttpPost getHttpPost(String base64Image, String prompt) {
        HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);

        httpPost.setHeader("Authorization", "Bearer " + Main.loadApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        // 2. 构建符合 DashScope 规范的请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen3-vl-flash");

        // 构建 Input 结构
        JSONObject input = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray content = new JSONArray();

        // 文本 Prompt：要求模型提取题目和答案
        JSONObject textItem = new JSONObject();
        textItem.put("text", prompt);

        // 图像 Item
        JSONObject imageItem = new JSONObject();
        imageItem.put("image", "data:image/png;base64," + base64Image);

        content.add(textItem);
        content.add(imageItem);
        message.put("content", content);
        messages.add(message);
        input.put("messages", messages);
        requestBody.put("input", input);

        // 发送请求
        StringEntity entity = new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return httpPost;
    }

    private String mergeResults(String id, List<CompletableFuture<JSONObject>> futures) throws Exception {
        JSONObject finalJson = new JSONObject();
        finalJson.put("id", id);
        finalJson.put("subject", "");
        JSONObject allQuestions = new JSONObject(new LinkedHashMap<>()); // 保持顺序

        for (CompletableFuture<JSONObject> future : futures) {
            JSONObject pageResult = future.get();
            if (finalJson.getString("subject").isEmpty()) {
                finalJson.put("subject", pageResult.getString("subject"));
            }
            allQuestions.putAll(pageResult.getJSONObject("questions"));
        }

        finalJson.put("questions", allQuestions);
        return finalJson.toJSONString();
    }

    private String processAnswerPdf(String pdfPath) throws Exception {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new FileNotFoundException("PDF文件不存在: " + pdfPath);
        }

        String answerId = "ANSWER_" + System.currentTimeMillis();

        if (getFileExtension(pdfPath).equals("pdf"))
        {// 1. 获取总页数
            int totalPages;
            try (PDDocument metaDoc = Loader.loadPDF(file)) {
                totalPages = metaDoc.getNumberOfPages();
            }

            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

            for (int i = 0; i < totalPages; i++) {
                final int pageIdx = i;

                CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(() -> {
                    // 【3.x 核心修复】：使用 RandomAccessReadBufferedFile 独立加载
                    // 这确保了每个线程拥有独立的文件流句柄和缓冲区，避免 NPE
                    try (RandomAccessReadBufferedFile raFile = new RandomAccessReadBufferedFile(file);
                         PDDocument document = Loader.loadPDF(raFile)) {

                        PDFRenderer renderer = new PDFRenderer(document);

                        // 渲染图片
                        BufferedImage pageImage = renderer.renderImageWithDPI(pageIdx, 144);

                        try {
                            if (pageImage == null) {
                                throw new RuntimeException("第 " + pageIdx + " 页渲染为空");
                            }
                            String pageBase64 = encodeImageToBase64(pageImage);
                            return callQwenVlApi(answerId, pageBase64, pageIdx, "answer");
//                        return new JSONObject();
                        } finally {
                            if (pageImage != null) {
                                pageImage.flush(); // 释放 BufferedImage 的 native 资源
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("处理第 " + pageIdx + " 页异常: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }, ThreadUtil.getExecutor());

                futures.add(future);
            }

            try {
                // 等待所有异步任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                return aggregateResults(answerId, futures);
            } catch (Exception e) {
                // 异常清理逻辑
                futures.forEach(f -> {
                    if (!f.isDone()) f.cancel(true);
                });
                throw new Exception("处理失败: " + e.getMessage(), e);
            }
        }
        else if (getFileExtension(pdfPath).equals("png")) {
            // 1. 基础校验
            if (!file.exists()) {
                throw new IOException("文件不存在或为空");
            }

            // 2. 使用 ImageIO 读取文件
            // 如果文件不是图片或格式不支持，read() 会返回 null
            BufferedImage image = ImageIO.read(file);

            // 3. 确保结果不为 null
            if (image == null) {
                throw new IOException("无法解码文件：该文件可能损坏或不是支持的图片格式");
            }

            List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

            CompletableFuture<JSONObject> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return callQwenVlApi(answerId, encodeImageToBase64(image), 0, "answer");
                }
                catch (Exception e) {
                    throw new RuntimeException("图片处理失败");
                }
            }, ThreadUtil.getExecutor());

            futures.add(future);

            return mergeResults(answerId, futures);
        }
        else {
            throw new IllegalArgumentException("文件类型不支持");
        }
    }

    private String aggregateResults(String id, List<CompletableFuture<JSONObject>> futures) throws Exception {
        JSONObject root = new JSONObject(new LinkedHashMap<>());
        root.put("id", id);
        root.put("subject", "");
        JSONObject allQuestions = new JSONObject(new LinkedHashMap<>());
        JSONObject allAnswers = new JSONObject(new LinkedHashMap<>());

        for (CompletableFuture<JSONObject> future : futures) {
            JSONObject pageData = future.get(); // 获取异步执行结果
            if (root.getString("subject").isEmpty()) {
                root.put("subject", pageData.getString("subject"));
            }
            allQuestions.putAll(pageData.getJSONObject("questions"));
            allAnswers.putAll(pageData.getJSONObject("answers"));
        }

        root.put("questions", allQuestions);
        root.put("answers", allAnswers);
        return JSON.toJSONString(root, true);
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // 1. 获取最后一个点的位置
        int lastDotIndex = filePath.lastIndexOf('.');

        // 2. 获取最后一个路径分隔符的位置（兼容 Windows 和 Unix）
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        // 3. 逻辑判断：
        // 点必须存在，且点必须在最后一个路径分隔符之后
        // 并且点不能是字符串的最后一个字符
        if (lastDotIndex > lastSeparatorIndex && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        }

        return ""; // 没有后缀名
    }

    private String ocrProcess(String path, String type) {
        // TODO: OCR 处理文件
        return "";
    }
}

package use_case.upload_student_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.StudentPaper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import use_case.Constants;
import use_case.dto.UploadStudentAnswerInputData;
import use_case.dto.UploadStudentAnswerOutputData;
import use_case.util.ApiUtil;
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
    public JSONObject processStudentDocument(String path, String studentPaperId, String examPaperId) throws Exception {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("文件不存在: " + path);

        String extension = FileUtil.getFileExtension(path).toLowerCase();

        // 步骤 1: 并发执行 OCR 识别（只识图，不请求 API）
        List<CompletableFuture<String>> ocrFutures = new ArrayList<>();

        if ("pdf".equals(extension)) {
            int pageCount;
            try (PDDocument metaDoc = PDDocument.load(file)) {
                pageCount = metaDoc.getNumberOfPages();
            }

            for (int i = 0; i < pageCount; i++) {
                final int pageIdx = i;
                ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                    // 使用临时文件模式加载，防止大 PDF 内存溢出
                    try (PDDocument document = PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())) {
                        PDFRenderer renderer = new PDFRenderer(document);
                        BufferedImage image = renderer.renderImageWithDPI(pageIdx, 144);
                        try {
                            if (image == null) throw new RuntimeException("第 " + pageIdx + " 页渲染为空");
                            return ocrProcess(image); // 仅返回 OCR 文本内容
                        } finally {
                            if (image != null) image.flush();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("处理第 " + pageIdx + " 页失败", e);
                    }
                }, ThreadUtil.getExecutor()));
            }
        } else if ("png".equals(extension) || "jpg".equals(extension)) {
            BufferedImage image = ImageIO.read(file);
            if (image == null) throw new IOException("无法解码图片");
            ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return ocrProcess(image);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    image.flush();
                }
            }, ThreadUtil.getExecutor()));
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 步骤 2: 等待所有页面的 OCR 任务完成
        CompletableFuture.allOf(ocrFutures.toArray(new CompletableFuture[0])).join();

        // 步骤 3: 按页码顺序汇总 OCR 结果
        StringBuilder fullOcrContent = new StringBuilder();
        for (int i = 0; i < ocrFutures.size(); i++) {
            fullOcrContent.append("--- Page ").append(i + 1).append(" ---\n");
            fullOcrContent.append(ocrFutures.get(i).get()).append("\n\n");
        }

        // 步骤 4: 构造完整的 Prompt 并调用一次 API
        String combinedPrompt = Constants.STUDENT_PROMPT +
                "\n\n【以下是该学生答卷整份文档的 OCR 识别内容，请结合上下文进行批改/识别】\n" +
                fullOcrContent.toString();

        JSONObject llmResponse = callQwenVlApi(combinedPrompt);

        // 步骤 5: 组装最终结果
        JSONObject result = new JSONObject(new LinkedHashMap<>());
        result.put("id", studentPaperId);
        result.put("examPaperId", examPaperId);
        result.put("subject", llmResponse.getString("subject") != null ? llmResponse.getString("subject") : "");
        result.put("responses", llmResponse.getJSONObject("responses"));
        result.put("questions", llmResponse.getJSONObject("questions"));
        result.put("coordContent", fullOcrContent.toString()); // 保留 OCR 汇总日志供溯源

        return result;
    }

    /**
     * 调用 Qwen API
     */
    private JSONObject callQwenVlApi(String combinedPrompt) throws Exception {
        // 设置较长的超时时间，因为多页汇总处理耗时较久
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(120000) // 120秒，适应长文本生成
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpPost httpPost = getHttpPost(combinedPrompt);

            String responseContent = httpClient.execute(httpPost, response -> {
                int status = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (status != 200) {
                    throw new RuntimeException("API 调用失败，状态码: " + status + "，详情: " + body);
                }
                return body;
            });

            return parseQwenResponse(responseContent);
        }
    }

    /**
     * 构建 HttpPost 请求对象
     */
    private HttpPost getHttpPost(String prompt) {
        HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
        httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen3-vl-flash"); // 或使用 qwen3-vl-max

        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray content = new JSONArray();
        // 只发送文本内容，因为 OCR 已经提供了视觉信息的文本描述
        // 如果需要发送图片，请参考原代码中注释的部分
        content.add(new JSONObject().fluentPut("type", "text").fluentPut("text", prompt));

        message.put("content", content);

        JSONObject input = new JSONObject();
        input.put("messages", Collections.singletonList(message));
        requestBody.put("input", input);

        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON));
        return httpPost;
    }

    /**
     * 解析 AI 返回的 Markdown JSON
     */
    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);

        // 兼容通义千问 API 的响应结构
        String contentText = responseObj.getJSONObject("output")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text");

        // 清理大模型可能返回的 Markdown 格式代码块标记
        String cleanJson = contentText.replaceAll("(?s)```json\\s*(.*?)\\s*```", "$1")
                .replaceAll("```", "")
                .trim();

        try {
            return JSON.parseObject(cleanJson);
        } catch (Exception e) {
            // 如果 JSON 解析失败，将原始文本存入，方便排查
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "JSON解析失败");
            errorObj.put("rawText", contentText);
            return errorObj;
        }
    }

    private String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String ocrProcess(BufferedImage image) throws Exception {
        // TODO
        return ApiUtil.getLLMResponseFromImage(image, Constants.OCR_PROMPT);
//        return Constants.TEST_OCR_RESPONSE;
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

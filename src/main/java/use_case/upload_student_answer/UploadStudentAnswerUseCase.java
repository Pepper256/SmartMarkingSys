package use_case.upload_student_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import entities.ExamPaper;
import entities.StudentPaper;
import org.apache.http.client.config.RequestConfig;
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
import use_case.util.LayoutConvertUtil;
import use_case.util.ThreadUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    private static class PageResult {
        public BufferedImage originImage;
        public List<Map<String, Object>> rawCells;
        public int inputWidth;
        public int inputHeight;
        public int pageIdx;

        public PageResult(BufferedImage originImage, List<Map<String, Object>> rawCells, int inputWidth, int inputHeight, int pageIdx) {
            this.originImage = originImage;
            this.rawCells = rawCells;
            this.inputWidth = inputWidth;
            this.inputHeight = inputHeight;
            this.pageIdx = pageIdx;
        }
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

        StringJoiner fullOcrContent = new StringJoiner("");

        // 步骤 1: 并发执行 OCR（返回 PageResult 对象）
        List<CompletableFuture<PageResult>> ocrFutures = new ArrayList<>();

        if ("pdf".equals(extension)) {
            int pageCount;
            try (PDDocument metaDoc = PDDocument.load(file)) {
                pageCount = metaDoc.getNumberOfPages();
            }

            for (int i = 0; i < pageCount; i++) {
                final int pageIdx = i;
                ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                    try (PDDocument document = PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())) {
                        PDFRenderer renderer = new PDFRenderer(document);
                        // 1. 获取原始图像 (originImage)
                        BufferedImage originImage = renderer.renderImageWithDPI(pageIdx, 144);
                        if (originImage == null) throw new RuntimeException("第 " + pageIdx + " 页渲染为空");

                        // 2. 获取模型输入的宽高 (inputWidth/Height)
                        int[] inputDims = LayoutConvertUtil.getResizedDimensions(originImage);

                        // 3. 调用大模型 API 获得原始 JSON String 并解析为 List<Map>
                        String ocrResult = ocrProcess(originImage);

                        fullOcrContent.add(ocrResult); // 保留ocr原始生成数据

                        List<Map<String, Object>> rawCells = LayoutConvertUtil.resultToCells(ocrResult);

                        return new PageResult(originImage, rawCells, inputDims[1], inputDims[0], pageIdx);
                    } catch (Exception e) {
                        throw new RuntimeException("处理第 " + pageIdx + " 页失败", e);
                    }
                }, ThreadUtil.getExecutor()));
            }
        } else if (List.of("png", "jpg", "jpeg").contains(extension)) {
            BufferedImage originImage = ImageIO.read(file);
            if (originImage == null) throw new IOException("无法解码图片");

            ocrFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    int[] inputDims = LayoutConvertUtil.getResizedDimensions(originImage);
                    String ocrResult = ocrProcess(originImage);
                    fullOcrContent.add(ocrResult);
                    List<Map<String, Object>> rawCells = LayoutConvertUtil.resultToCells(ocrResult);
                    return new PageResult(originImage, rawCells, inputDims[1], inputDims[0], 0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, ThreadUtil.getExecutor()));
        }

// 步骤 2: 等待并发任务结束
        CompletableFuture.allOf(ocrFutures.toArray(new CompletableFuture[0])).join();

// 步骤 3: 顺序执行坐标还原并生成 Markdown
        StringBuilder fullMarkdown = new StringBuilder();

        HashMap<String, String> paperBase64 = new HashMap<>();

        for (int i = 0; i < ocrFutures.size(); i++) {
            PageResult res = ocrFutures.get(i).get();

            // 3.1 坐标还原 (Post-process)
            List<Map<String, Object>> processedCells = LayoutConvertUtil.postProcessCells(
                    res.originImage,
                    res.rawCells,
                    res.inputWidth,
                    res.inputHeight
            );

            // 3.2 转换为 Markdown
            String pageMd = LayoutConvertUtil.layoutJson2Md(res.originImage, processedCells, "text", true);

            fullMarkdown.append("\n");
            fullMarkdown.append(pageMd).append("\n\n");

            // 3.3 及时释放图片内存
            res.originImage.flush();

            paperBase64.put(Integer.toString(i), LayoutConvertUtil.imageToBase64(res.originImage));
        }

        String finalResult = fullMarkdown.toString();
        ExamPaper examPaper = dao.getExamPaperById(examPaperId);
        // 步骤 4: 构造完整的 Prompt 并调用一次 API
        String combinedPrompt = Constants.STUDENT_PROMPT +
                "[Standard_Keys]" +
                examPaper.getQuestions().keySet().toString() +
                "\n\n[Student_Card_OCR]\n\n" +
                finalResult;

        JSONObject llmResponse = callDeepseekApi(combinedPrompt);

        // 步骤 5: 组装最终结果
        JSONObject result = new JSONObject(new LinkedHashMap<>());
        result.put("id", studentPaperId);
        result.put("examPaperId", examPaperId);
        result.put("subject", llmResponse.getString("subject") != null ? llmResponse.getString("subject") : "");
        result.put("responses", llmResponse.getJSONObject("responses"));
        result.put("questions", examPaper.getQuestions());
        result.put("coordContent", fullOcrContent.toString()); // 保留 OCR 汇总日志供溯源
        result.put("paperBase64", paperBase64);

        return result;
    }

    /**
     * 调用 Qwen API
     */
    private JSONObject callDeepseekApi(String combinedPrompt) throws Exception {
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
        HttpPost httpPost = new HttpPost(Constants.DEEPSEEK_API_URL);
        httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", Constants.DEEPSEEK_API_MODEL); // 或使用 qwen3-vl-max
//        requestBody.put("enable_thinking", true);
        requestBody.put("temperature", 0);

        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray content = new JSONArray();
        // 只发送文本内容，因为 OCR 已经提供了视觉信息的文本描述
        // 如果需要发送图片，请参考原代码中注释的部分
        content.add(new JSONObject().fluentPut("type", "text").fluentPut("text", prompt));

        message.put("content", content);

//        JSONObject input = new JSONObject();
//        input.put("messages", Collections.singletonList(message));
//        requestBody.put("input", input);
        requestBody.put("messages", Collections.singletonList(message));

        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON));
        return httpPost;
    }

    /**
     * 解析 AI 返回的 Markdown JSON
     */
    private JSONObject parseQwenResponse(String rawJson) {
        JSONObject responseObj = JSON.parseObject(rawJson);

        // 兼容通义千问 API 的响应结构
        String contentText = responseObj//.getJSONObject("output")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
//                .getJSONObject(0)
//                .getString("text");

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

    private String ocrProcess(BufferedImage image) throws Exception {
        return ApiUtil.getOCRResponseFromImage(image, Constants.OCR_PROMPT);
//        return Constants.TEST_STUDENT_OCR_RESPONSE;
    }

}

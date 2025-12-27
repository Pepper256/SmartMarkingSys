package use_case.generate_student_report;

import app.Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.MarkedStudentPaper;
import entities.Report;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import use_case.Constants;
import use_case.dto.GenerateStudentReportInputData;
import use_case.dto.GenerateStudentReportOutputData;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class GenerateStudentReportUseCase implements GenerateStudentReportInputBoundary {

    private final GenerateStudentReportOutputBoundary outputBoundary;
    private final GenerateStudentReportDataAccessInterface dao;
    private final ObjectMapper objectMapper;

    public GenerateStudentReportUseCase(GenerateStudentReportOutputBoundary outputBoundary,
                                        GenerateStudentReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(GenerateStudentReportInputData inputData) {
        String markedStudentPaperId = inputData.getMarkedStudentPaperId();
        MarkedStudentPaper paper = dao.getMarkedStudentPaperById(markedStudentPaperId);

        if (paper == null) {
            outputBoundary.prepareFailView(new GenerateStudentReportOutputData(""));
            return;
        }

        try {
            // 1. 整理题目数据
            String questionDetails = formatQuestionDetails(paper);

            // 2. 组装最终 Prompt
            String finalPrompt = String.format(Constants.REPORT_PROMPT, paper.getSubject(), questionDetails);

            // 3. 发起原生 HTTP 请求调用 Qwen-VL-Flash
            String generatedReport = callQwenVlFlashApi(paper.getMarkedContent(), finalPrompt);

            // 4. 创建 Report 对象并持久化
            Report report = new Report(
                    "report_" + UUID.randomUUID().toString(),
                    paper.getId(),
                    generatedReport
            );
            dao.storeReport(report);

            // 5. 调用成功视图
            GenerateStudentReportOutputData outputData = new GenerateStudentReportOutputData(generatedReport);
            outputBoundary.prepareSuccessView(outputData);

        } catch (Exception e) {
            // 捕获所有可能的网络、解析或逻辑异常
            outputBoundary.prepareFailView(new GenerateStudentReportOutputData(""));
        }
    }

    private String formatQuestionDetails(MarkedStudentPaper paper) {
        StringBuilder sb = new StringBuilder();
        paper.getQuestions().forEach((id, question) -> {
            String response = paper.getResponses().getOrDefault(id, "未作答");
            boolean isCorrect = paper.getCorrectness().getOrDefault(id, false);
            String reason = paper.getReasons().getOrDefault(id, "无详细解析");

            sb.append(String.format("- 题目: %s\n  回答: %s\n  判题: %s\n  原因: %s\n\n",
                    question, response, isCorrect ? "正确 ✅" : "错误 ❌", reason));
        });
        return sb.toString();
    }

    private String callQwenVlFlashApi(String imageUrl, String prompt) throws IOException {
        // 建议在 DAO 或环境变量中管理 API Key
        String apiKey = Main.loadQwenApiKey();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            // 构造符合 Qwen-VL 规范的 JSON 结构
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "qwen-vl-flash");

            ObjectNode inputNode = rootNode.putObject("input");
            ArrayNode messages = inputNode.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");

            ArrayNode contents = userMessage.putArray("content");
            contents.addObject().put("image", imageUrl); // 传入批改后的图片
            contents.addObject().put("text", prompt);     // 传入分析指令

            httpPost.setEntity(new StringEntity(rootNode.toString(), ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("API 响应异常 [Code: " + response.getStatusLine().getStatusCode() + "]: " + responseBody);
                }

                // 解析 JSON 路径: output -> choices[0] -> message -> content[0] -> text
                return objectMapper.readTree(responseBody)
                        .path("output")
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .get(0)
                        .path("text")
                        .asText();
            });
        }
    }
}

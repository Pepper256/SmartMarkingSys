package use_case.generate_student_report;

import app.Main;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Collections;
import java.util.HashMap;
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
            String finalPrompt = Constants.REPORT_PROMPT + "\n以下为试卷内容\n" + questionDetails;

            // 3. 发起原生 HTTP 请求调用 Qwen-VL-Flash
            String generatedReport = callQwenVlFlashApi(finalPrompt);

            // 4. 创建 Report 对象并持久化
            Report report = new Report(
                    "report_" + UUID.randomUUID().toString(),
                    paper.getExamPaperId(),
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
        HashMap<String, String> questions = paper.getQuestions();
        HashMap<String, Boolean> correctness = paper.getCorrectness();
        HashMap<String, String> responses = paper.getResponses();

        JSONObject jsonObject = new JSONObject();
        for(String key : questions.keySet()) {
            JSONObject temp = new JSONObject();
            temp.put("question", questions.get(key));
            temp.put("correctness", correctness.getOrDefault(key, false));
            temp.put("response", responses.getOrDefault(key, ""));
            jsonObject.put(key, temp);
        }
        return jsonObject.toJSONString();
    }

    private String callQwenVlFlashApi(String prompt) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = getHttpPost(prompt);

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
}

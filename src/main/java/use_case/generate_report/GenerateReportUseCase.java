package use_case.generate_report;

import app.Main;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.Output;
import entities.MarkedStudentPaper;
import entities.Report;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import use_case.Constants;
import use_case.dto.GenerateReportInputData;
import use_case.dto.GenerateReportOutputData;
import use_case.dto.GenerateStudentReportOutputData;

import java.io.IOException;
import java.util.*;

public class GenerateReportUseCase implements GenerateReportInputBoundary{

    private final GenerateReportOutputBoundary outputBoundary;
    private final GenerateReportDataAccessInterface dao;
    private final ObjectMapper objectMapper;

    public GenerateReportUseCase(GenerateReportOutputBoundary outputBoundary,
                                 GenerateReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(GenerateReportInputData inputData) {
        String examPaperId = inputData.getExamPaperId();

        List<MarkedStudentPaper> markedStudentPapers = dao.getMarkedStudentPapersByExamPaperId(examPaperId);

        if(markedStudentPapers.isEmpty()) {
            outputBoundary.prepareFailView(new GenerateReportOutputData(""));
        }

        String finalPrompt;

        List<String> questionDetails = new ArrayList<>();

        for(MarkedStudentPaper paper : markedStudentPapers) {
            questionDetails.add(formatQuestionDetails(paper));
        }

        try {
            finalPrompt = Constants.REPORT_PROMPT + "\n以下本次考试学生的批改后的试卷内容\n" + objectMapper.writeValueAsString(questionDetails);

            // 3. 发起原生 HTTP 请求调用 Qwen-VL-Flash
            String generatedReport = callQwenVlFlashApi(finalPrompt);

            // 4. 创建 Report 对象并持久化
            Report report = new Report(
                    "report_" + UUID.randomUUID().toString(),
                    examPaperId,
                    null,
                    generatedReport
            );
            dao.storeReport(report);
        }
        catch (Exception e) {
            System.out.println("api调用失败");
            outputBoundary.prepareFailView(new GenerateReportOutputData(""));
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

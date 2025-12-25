package use_case.auto_marking;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.MarkedStudentPaper;
import entities.StudentPaper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import use_case.Constants;
import use_case.dto.AutoMarkingInputData;
import use_case.dto.AutoMarkingOutputData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoMarkingUseCase implements AutoMarkingInputBoundary{

    private final AutoMarkingOutputBoundary outputBoundary;
    private final AutoMarkingDataAccessInterface dao;

    public AutoMarkingUseCase(AutoMarkingOutputBoundary outputBoundary,
                              AutoMarkingDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(AutoMarkingInputData inputData) {
        List<String> ids = inputData.getStudentPaperIds();

        List<MarkedStudentPaper> markedPapers = new ArrayList<>();
        List<String> markedContentWithCoords = new ArrayList<>();
        for (String id : ids) {
            StudentPaper studentPaper = dao.getStudentPaperById(id);

            // 构建问题答案键值对字符串
            HashMap<String, HashMap<String, String>> map = new HashMap<>();
            for (String key : studentPaper.getQuestions().keySet()) {
                HashMap<String, String> temp = new HashMap<String, String>();
                temp.put("question", studentPaper.getQuestions().get(key));
                temp.put("response", studentPaper.getResponses().get(key));
                map.put(key, temp);
            }
            String content = JSON.toJSONString(map);

            try {
                String jsonWithMarkedContent = askDeepSeek(content + "\n" +studentPaper.getCoordContent());
                JSONObject temp = JSON.parseObject(jsonWithMarkedContent);
                JSONObject answerInfo = temp.getJSONObject("answerInfo");

                HashMap<String, Boolean> correctness = new HashMap<>();
                HashMap<String, String> reasons = new HashMap<>();
                for (String key : answerInfo.keySet()) {
                    correctness.put(key, answerInfo.getBoolean("marked"));
                    reasons.put(key, answerInfo.getString("reason"));
                }


                markedPapers.add(new MarkedStudentPaper(
                        studentPaper,
                        correctness,
                        temp.getJSONObject("markWithCoords").toJSONString(),
                        reasons)
                );
                markedContentWithCoords.add(jsonWithMarkedContent);
            }
            catch (Exception e) {
                outputBoundary.prepareFailView(new AutoMarkingOutputData(new ArrayList<String>()));
            }
        }

        dao.storeMarkedPapers(markedPapers);

        outputBoundary.prepareSuccessView(new AutoMarkingOutputData(markedContentWithCoords));
    }

    private String askDeepSeek(String content) throws Exception {
        // 1. 在函数内部实例化 Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // 2. 构建 JSON 请求体
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", "deepseek-chat");
        rootNode.putArray("messages")
                .addObject()
                .put("role", "user")
                .put("content", Constants.MARKING_PROMPT + content);
        rootNode.put("stream", false);

        String jsonPayload = mapper.writeValueAsString(rootNode);

        // 3. 在函数内部实例化 Apache HttpClient
        // 使用 try-with-resources 确保 HttpClient 和 HttpResponse 自动关闭，防止连接泄露
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(Constants.DEEPSEEK_API_URL);

            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + Main.loadDeepseekApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            // 设置实体内容
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            // 4. 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    // 5. 解析响应内容
                    JsonNode responseJson = mapper.readTree(responseBody);
                    return responseJson.path("choices")
                            .get(0)
                            .path("message")
                            .path("content")
                            .asText();
                } else {
                    throw new IOException("API 错误，状态码: " + statusCode + " 响应: " + responseBody);
                }
            }
        }
    }
}

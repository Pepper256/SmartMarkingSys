package use_case.auto_marking;

import app.Main;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import use_case.util.ThreadUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        // 1. 并行批改试卷
        List<CompletableFuture<MarkingResult>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> processSinglePaper(id), ThreadUtil.getExecutor()))
                .collect(Collectors.toList());

        try {
            // 2. 等待所有批改任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<MarkedStudentPaper> markedPapers = new ArrayList<>();
            List<String> rawContents = new ArrayList<>();

            for (CompletableFuture<MarkingResult> future : futures) {
                MarkingResult result = future.get();
                if (result != null) {
                    markedPapers.add(result.markedPaper);
                    rawContents.add(result.rawJson);
                }
            }

            // 3. 持久化并返回
            dao.storeMarkedPapers(markedPapers);
            outputBoundary.prepareSuccessView(new AutoMarkingOutputData(rawContents));

        } catch (Exception e) {
            e.printStackTrace();
            outputBoundary.prepareFailView(new AutoMarkingOutputData(new ArrayList<>()));
        }
    }

    /**
     * 处理单份试卷的批改逻辑
     */
    private MarkingResult processSinglePaper(String id) {
        try {
            StudentPaper studentPaper = dao.getStudentPaperById(id);
            if (studentPaper == null) return null;

            // 构建批改请求内容：问题 + 学生回答 + OCR坐标参考
            Map<String, Object> markContext = new HashMap<>();
            for (String key : studentPaper.getQuestions().keySet()) {
                String question = studentPaper.getQuestions().get(key);
                Map<String, String> questionAndResponse = new HashMap<>();
                try {
                    String response = studentPaper.getResponses().get(key);
                    questionAndResponse.put("question", question);
                    questionAndResponse.put("response", response);
                    markContext.put(key, questionAndResponse);
                }
                catch (Exception e) {
                    questionAndResponse.put("question", question);
                    questionAndResponse.put("response", "");
                }
                markContext.put(key, questionAndResponse);
            }

            String promptPayload = "json1 = " +
                    JSON.toJSONString(markContext) +
                    "\n【OCR坐标上下文】\njson2 = " +
                    studentPaper.getCoordContent() +
                    "json3=" +
                    dao.getAnswerPaperByExamPaperId(studentPaper.getExamPaperId()).getAnswers();

            // 调用通用的 Qwen 处理逻辑
            String responseJsonStr = askQwen(promptPayload);
            JSONObject root = JSON.parseObject(responseJsonStr);

            // 解析批改详情
            JSONObject answerInfo = root.getJSONObject("answerInfo");
            HashMap<String, Boolean> correctness = new HashMap<>();
            HashMap<String, String> reasons = new HashMap<>();

            for (String key : answerInfo.keySet()) {
                JSONObject detail = answerInfo.getJSONObject(key);
                correctness.put(key, detail.getBoolean("correctness"));
                reasons.put(key, detail.getString("reason"));
            }

            // 封装结果
            MarkedStudentPaper markedPaper = new MarkedStudentPaper(
                    studentPaper,
                    correctness,
                    root.getJSONObject("markWithCoords").toJSONString(),
                    reasons
            );

            return new MarkingResult(markedPaper, root.getJSONObject("markWithCoords").toJSONString());
        } catch (Exception e) {
            throw new RuntimeException("试卷 " + id + " 批改失败", e);
        }
    }

    /**
     * 调用 Qwen 模型进行文本推理（批改逻辑）
     */
    private String askQwen(String content) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
            httpPost.setHeader("Content-Type", "application/json");

            // 构建 DashScope 规范的请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen3-vl-flash");

            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray messagesContent = new JSONArray();
            messagesContent.add(new JSONObject().fluentPut("text", Constants.MARKING_PROMPT + content));

            message.put("content", messagesContent);
            requestBody.put("input", new JSONObject().fluentPut("messages", Collections.singletonList(message)));

            return httpClient.execute(httpPost, response -> {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Qwen API Error: " + response.getStatusLine().getStatusCode() + " - " + body);
                }

                // 解析并清洗返回的 JSON 字符串
                JSONObject resObj = JSON.parseObject(body);
                String text = resObj.getJSONObject("output")
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getJSONArray("content")
                        .getJSONObject(0)
                        .getString("text");

                return text.replaceAll("```json", "").replaceAll("```", "").trim();
            });
        }
    }

    /**
     * 内部类用于承载多线程执行结果
     */
    private static class MarkingResult {
        final MarkedStudentPaper markedPaper;
        final String rawJson;

        MarkingResult(MarkedStudentPaper markedPaper, String rawJson) {
            this.markedPaper = markedPaper;
            this.rawJson = rawJson;
        }
    }
}

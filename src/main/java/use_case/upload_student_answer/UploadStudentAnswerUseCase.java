package use_case.upload_student_answer;

import app.Main;
import com.alibaba.fastjson.JSON;
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
import use_case.Constants;
import use_case.dto.UploadStudentAnswerInputData;
import use_case.dto.UploadStudentAnswerOutputData;
import use_case.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        for (String path : paths) {
            try {
                if (FileUtil.getFileExtension(path).equals("pdf") ||
                        FileUtil.getFileExtension(path).equals("png")) {

                    String processedContent = ocrProcess(path);
                    String studentAnswer = askDeepSeek(processedContent);

                    JSONObject temp = JSON.parseObject(studentAnswer);
                    temp.put("coordContent", processedContent);
                    temp.put("examPaperId", examPaperId);

                    papers.add(StudentPaper.jsonToStudentPaper(studentAnswer));
                } else {
                    throw new IllegalArgumentException("文件类型不支持");
                }
            }
            catch (Exception e) {
                outputBoundary.prepareFailView(new UploadStudentAnswerOutputData());
            }
        }

        dao.saveStudentPapers(papers);

        outputBoundary.prepareSuccessView(new UploadStudentAnswerOutputData());
    }

    /**
     * @param path 文件路径
     * @return ocr处理过后的手写识别字符串
     */
    private String ocrProcess(String path) {
        // TODO
        return "";
    }

    public static String askDeepSeek(String content) throws Exception {
        // 1. 在函数内部实例化 Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // 2. 构建 JSON 请求体
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", "deepseek-chat");
        rootNode.putArray("messages")
                .addObject()
                .put("role", "user")
                .put("content", Constants.STUDENT_PROMPT + content);
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

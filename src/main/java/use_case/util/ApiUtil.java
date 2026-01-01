package use_case.util;

import app.Main;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import use_case.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

public class ApiUtil {

    public static HttpPost getHttpPost(String prompt) {
        HttpPost httpPost = new HttpPost(Constants.DEEPSEEK_API_URL);
        httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", Constants.DEEPSEEK_API_MODEL);
//        requestBody.put("enable_thinking", true);
        requestBody.put("temperature", 0.8);

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

    public static String callDeepseekApi(String prompt) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = ApiUtil.getHttpPost(prompt);

            return httpClient.execute(httpPost, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("API 响应异常 [Code: " + response.getStatusLine().getStatusCode() + "]: " + responseBody);
                }

                // 解析 JSON 路径: output -> choices[0] -> message -> content[0] -> text
                String text = objectMapper.readTree(responseBody)
//                        .path("output")
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content").asText();
//                        .get(0)
//                        .path("text")
//                        .asText();
                return text.replaceAll("```json", "").replaceAll("```", "").trim();
            });
        }
    }

    public static String getOCRResponseFromImage(BufferedImage image, String prompt) {
        ObjectMapper mapper = new ObjectMapper();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(Constants.OCR_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + Constants.OCR_API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            // --- 构建 JSON Payload ---
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("model", Constants.OCR_MODEL_NAME);
            rootNode.put("temperature", Constants.DEFAULT_TEMP);
            rootNode.put("top_p", Constants.DEFAULT_TOP_P);
            rootNode.put("max_completion_tokens", Constants.MAX_TOKENS);

            ArrayNode messages = rootNode.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");

            ArrayNode contentArray = userMessage.putArray("content");

            // 1. 集成 Python PILimage_to_base64 的逻辑：添加 Data URI 前缀
            // 这里调用了您的 encodeImageToBase64 但在外部包装了前缀
            String base64Data = encodeImageToBase64(image);
            String dataUri = "data:image/png;base64," + base64Data;

            ObjectNode imagePart = contentArray.addObject();
            imagePart.put("type", "image_url");
            imagePart.putObject("image_url").put("url", dataUri);

            // 2. 文本部分 (保留特殊的 vLLM Token 拼接)
            ObjectNode textPart = contentArray.addObject();
            textPart.put("type", "text");
            textPart.put("text", String.format("<|img|><|imgpad|><|endofimg|>%s", prompt));

            // 设置 Entity
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(rootNode), ContentType.APPLICATION_JSON));

            // --- 执行请求 ---
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    ObjectNode responseJson = (ObjectNode) mapper.readTree(responseBody);
                    return responseJson.get("choices").get(0).get("message").get("content").asText();
                } else {
                    System.err.println("Error: " + response.getStatusLine().getStatusCode() + " - " + responseBody);
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
            return null;
        }
    }

    private static String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}

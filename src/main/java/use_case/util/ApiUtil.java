package use_case.util;

import app.Main;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

public class ApiUtil {

    public static HttpPost getHttpPost(String prompt) {
        HttpPost httpPost = new HttpPost(Constants.QWEN_API_URL);
        httpPost.setHeader("Authorization", "Bearer " + Main.loadQwenApiKey());
        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", Constants.API_MODEL);

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

    public static String callQwenVlFlashApi(String prompt) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = ApiUtil.getHttpPost(prompt);

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

    public static String getLLMResponseFromImage(BufferedImage image, String prompt) {
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

    private static String encodeImageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}

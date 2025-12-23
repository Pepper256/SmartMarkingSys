package use_case;

public class Constants {

    public static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    public static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";

    public static final String DAO_PATH = "./src/main/resources/database/data.json";

    public static final String ANSWER_PROMPT = "你是一个高精度的 OCR 试卷识别助手。请分析图片并输出 JSON：" +
            "{\"subject\": \"学科\", \"questions\": {\"题号\": \"内容\"}, \"answers\": {\"题号\": \"解析或答案\"}}。" +
            "如果题目或答案包含图片，请将图片内容描述清楚并在位置标注 [IMAGE]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
    public static final String EXAM_PROMPT = "你是一个老师。请识别图中试卷内容。输出JSON格式：{" +
            "\"subject\": \"学科名\", " +
            "\"questions\": {\"题号\": \"题目内容\"}" +
            "}。若题目有图，请保留描述并在内容中包含[IMAGE:base64]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
    public static final String STUDENT_PROMPT = "你是一个老师。请识别文本中已作答的试卷内容。输出JSON格式：{" +
            "\"subject\": \"学科名\", " +
            "\"questions\": {\"题号\": \"题目内容\"}" +
            "\"responses\": {\"题号\": \"回答内容\"}" +
            "}。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
    public static final String MARKING_PROMPT = "你是一个老师。请识别图中试卷内容并批改试卷。" +
            "第一个json包含题目和学生答案的键值对，其中键为题目值为答案。第二个为包含坐标信息的学生作答试卷。" +
            "第二个json包含坐标信息，你需要推理并批改对应学生作答的部分。" +
            "输出JSON格式，包含两个字段：{" +
            "\"answerInfo\": 值为第一个输入的json，其中键为题号，值为一个嵌套json，对于该嵌套json键为问题描述值为学生答案，在每一个嵌套json内加入两个字段: " +
            "\"marked\":true或false，代表该题正确性。" +
            "\"reason\":如果该题正确，则值为空，如果该题错误，则分析错误原因并将分析出的错因作为该字段的值。" +
            "输出的json的第一个字段的说明到此结束，接下来为第二个字段的说明," +
            "\"markWithCoords\":第二个输入的json，保持该json格式不变，在每一个可能为学生回答的json对象内添加新的字段，\"marked\":true或false，代表该题正确性。" +
            "}，若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
}

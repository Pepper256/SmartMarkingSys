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
            "\"questions\": {\"题号\": \"题目内容\"}," +
            "\"responses\": {\"题号\": \"回答内容\"}," +
            "}。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
}

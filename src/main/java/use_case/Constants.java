package use_case;

import java.nio.file.Paths;

public class Constants {

    public static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    public static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    public static final String OCR_API_URL = ""; // TODO

    public static final String DAO_PATH = "./src/main/resources/database/data.json";

    public static final String OCR_MODEL_NAME = "rednote-hilab/dots.ocr";

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
    public static final String REPORT_PROMPT = """
            你是一位专业的AI助教。请根据以下已批改试卷的数据及图片内容，生成一份详细的学习总结报告。

            【科目】：%s

            【学生答题详情】：
            %s

            【报告要求】：
            1. 分析学生在各个知识点上的掌握情况。
            2. 总结典型的错误类型（如：概念模糊、计算失误、审题不严）。
            3. 结合批改图片中的痕迹，评价学生的书写或解题过程。
            4. 提供不少于3条具体的后续学习建议。

            请直接输出报告正文，不要包含多余的开场白。
            """;

    public static final String DOWNLOAD_PATH = Paths.get(System.getProperty("user.home"), "Downloads").toString();
    public static final String OCR_PROMPT = "Please output the layout information from the PDF image, including each layout element's bbox, its category, and the corresponding text content within the bbox.\n" +
            "\n" +
            "1. Bbox format: [x1, y1, x2, y2]\n" +
            "\n" +
            "2. Layout Categories: The possible categories are ['Caption', 'Footnote', 'Formula', 'List-item', 'Page-footer', 'Page-header', 'Picture', 'Section-header', 'Table', 'Text', 'Title'].\n" +
            "\n" +
            "3. Text Extraction & Formatting Rules:\n" +
            "    - Picture: For the 'Picture' category, the text field should be omitted.\n" +
            "    - Formula: Format its text as LaTeX.\n" +
            "    - Table: Format its text as HTML.\n" +
            "    - All Others (Text, Title, etc.): Format their text as Markdown.\n" +
            "\n" +
            "4. Constraints:\n" +
            "    - The output text must be the original text from the image, with no translation.\n" +
            "    - All layout elements must be sorted according to human reading order.\n" +
            "\n" +
            "5. Final Output: The entire output must be a single JSON object.";
}

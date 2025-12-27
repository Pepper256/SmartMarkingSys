package use_case;

import java.nio.file.Paths;

public class Constants {

    public static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    public static final String OCR_API_URL = ""; // TODO

    public static final String DAO_PATH = "./src/main/resources/database/data.json";

    public static final String OCR_MODEL_NAME = "rednote-hilab/dots.ocr";
    public static final String OCR_API_KEY = "random666ydbyl";

    public static final String ANSWER_PROMPT = "你是一个高精度的 OCR 试卷识别助手。请分析图片并输出 JSON：" +
            "{\"subject\": \"学科\", \"questions\": {\"题号\": \"内容\"}, \"answers\": {\"题号\": \"解析或答案\"}}。" +
            "如果题目或答案包含图片，请将图片内容描述清楚并在位置标注 [IMAGE:<对应的base64字符串>]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
    public static final String EXAM_PROMPT = "你是一个老师。请识别图中试卷内容。输出JSON格式：{" +
            "\"subject\": \"学科名\", " +
            "\"questions\": {\"题号\": \"题目内容\"}" +
            "}。若题目有图，请保留描述并在内容中包含[IMAGE:<对应的base64字符串>]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增";
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


    // TODO: delete this when ocr is integrated
    public static final String TEST_OCR_RESPONSE = "[\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      493,\n" +
            "      131,\n" +
            "      1158,\n" +
            "      286\n" +
            "    ],\n" +
            "    \"category\": \"Title\",\n" +
            "    \"text\": \"# 2023 北京朝阳初二(下)期末\\n## 道德与法治\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      321,\n" +
            "      1488,\n" +
            "      414\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      436,\n" +
            "      483,\n" +
            "      472\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"A. 首次实现空间交会对接\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      495,\n" +
            "      480,\n" +
            "      530\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"B. 首次多人多天载人飞行\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      552,\n" +
            "      615,\n" +
            "      588\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"C. 航天员乘组首次实现“太空会师”\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      611,\n" +
            "      745,\n" +
            "      646\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"D. 航天飞行工程师和载荷专家的首次太空飞行\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      669,\n" +
            "      1169,\n" +
            "      704\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      153,\n" +
            "      728,\n" +
            "      1312,\n" +
            "      1120\n" +
            "    ],\n" +
            "    \"category\": \"Picture\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1145,\n" +
            "      687,\n" +
            "      1181\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"A. 宪法规定实现公民基本权利的保障措施\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1204,\n" +
            "      888,\n" +
            "      1239\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"B. 宪法与我们息息相关,我们的一生都离不开宪法的保护\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1261,\n" +
            "      595,\n" +
            "      1297\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"C. 宪法规定国家生活中的根本问题\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1320,\n" +
            "      656,\n" +
            "      1355\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"D. 一切权力属于人民是我国的宪法原则\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1378,\n" +
            "      1198,\n" +
            "      1413\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1434,\n" +
            "      616,\n" +
            "      1468\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"①彤彤上课认真听讲,按时完成作业\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1491,\n" +
            "      644,\n" +
            "      1526\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"②哥哥将压岁钱存到银行,并获得利息\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1549,\n" +
            "      879,\n" +
            "      1584\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1607,\n" +
            "      703,\n" +
            "      1641\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"④妈妈参加了新一届朝阳区人大代表的选举\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1662,\n" +
            "      249,\n" +
            "      1697\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"A. ①②\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      468,\n" +
            "      1662,\n" +
            "      567,\n" +
            "      1697\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"B. ①④\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      730,\n" +
            "      1662,\n" +
            "      830,\n" +
            "      1697\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"C. ②③\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      993,\n" +
            "      1662,\n" +
            "      1093,\n" +
            "      1697\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"D. ③④\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      147,\n" +
            "      1720,\n" +
            "      585,\n" +
            "      1755\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"4. 如图中①、②两处应填( )\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      153,\n" +
            "      1780,\n" +
            "      1022,\n" +
            "      2082\n" +
            "    ],\n" +
            "    \"category\": \"Picture\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      741,\n" +
            "      2299,\n" +
            "      911,\n" +
            "      2333\n" +
            "    ],\n" +
            "    \"category\": \"Page-footer\",\n" +
            "    \"text\": \"第1页/共12页\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      724,\n" +
            "      131,\n" +
            "      928,\n" +
            "      185\n" +
            "    ],\n" +
            "    \"category\": \"Title\",\n" +
            "    \"text\": \"参考答案\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      225,\n" +
            "      315,\n" +
            "      260\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"1.【答案】D\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      281,\n" +
            "      1493,\n" +
            "      487\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"【解析】2023年5月30日,搭载神舟十六号载人飞船的长征二号F运载火箭,在酒泉卫星发射中心点火升空,成功将航天员景海鹏、朱杨柱、桂海潮顺利送入太空。此次神舟十六号航天员乘组首次包含“航天驾驶员、航天飞行工程师、载荷专家”3种航天员类型,也是我国航天飞行工程师和载荷专家的首次太空飞行。故D符合题意;ABC不符合题意。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      509,\n" +
            "      272,\n" +
            "      543\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"故选：D。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      565,\n" +
            "      805,\n" +
            "      600\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题考查时事政治。具体涉及神舟十六号载人飞船。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      622,\n" +
            "      1126,\n" +
            "      657\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"解答该题要多关注时事,平时要注意养成关心国家大事的习惯,多了解时事。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      680,\n" +
            "      311,\n" +
            "      715\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"2.【答案】B\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      736,\n" +
            "      1493,\n" +
            "      885\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"【解析】分析漫画可知,体现了宪法与我们息息相关,我们的一生都离不开宪法的保护,宪法规定公民基本权利,公民行使权利要遵守宪法和法律,宪法规定公民基本义务,法定义务必须履行,维护宪法尊严就要捍卫宪法,勇于同违反宪法的行为作斗争,B是正确的选项;ACD不符合题意。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      906,\n" +
            "      272,\n" +
            "      941\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"故选：B。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      964,\n" +
            "      1449,\n" +
            "      999\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题考查了宪法是国家的根本法。宪法是国家的根本法，具有最高的法律效力，是治国安邦的总章程。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1021,\n" +
            "      1461,\n" +
            "      1111\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题要正确理解题意,只有理解题意,才能明确考查的知识点是宪法是国家的根本法,才能做出正确选择。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1135,\n" +
            "      315,\n" +
            "      1170\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"3.【答案】D\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1192,\n" +
            "      1493,\n" +
            "      1340\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"【解析】题干中,爸爸向人大代表反映小区增设新能源汽车充电桩的问,行使了监督权;妈妈参加了新一届朝阳区人大代表的选举,行使了选举权和被选举权,选举权和被选举权属于政治权利和自由,③④说法正确;①属于行使受教育权;②属于行使财产的收益权。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1362,\n" +
            "      272,\n" +
            "      1397\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"故选：D。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1419,\n" +
            "      1493,\n" +
            "      1509\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题考查正确行使政治权利。公民应当珍惜并正确行使民主政治权利，每个公民应该树立主人翁意识，珍惜宪法和法律赋予的各项政治权利及自由。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1531,\n" +
            "      1331,\n" +
            "      1566\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题属基础知识题，考查正确行使政治权利，根据教材知识，依据题意分析，选出正确答案。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1590,\n" +
            "      315,\n" +
            "      1624\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"4.【答案】D\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1646,\n" +
            "      1493,\n" +
            "      1795\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"【解析】我国是人民民主专政的国家，一切权力属于人民，人民选举代表组成各级人民代表大会，集中行使国家权力，再由人民代表大会产生行政、审判、检察、监察等机关，分别行使管理国家、维护社会秩序等权利，它们都对人大负责，受人大监督，D是正确的选项；ABC描述都不正确。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1816,\n" +
            "      272,\n" +
            "      1851\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"故选：D。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1873,\n" +
            "      1493,\n" +
            "      1964\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"本题考查了人民代表大会制度。人民代表大会制度是符合中国国情和实际、体现社会主义国家性质、保证人民当家作主、保障实现中华民族伟大复兴的好制度。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      1985,\n" +
            "      1449,\n" +
            "      2020\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"解答本题需明确考查的知识点是人民代表大会制度，在此基础上，结合分析各个说法，选出正确答案。\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      149,\n" +
            "      2044,\n" +
            "      315,\n" +
            "      2079\n" +
            "    ],\n" +
            "    \"category\": \"Text\",\n" +
            "    \"text\": \"5.【答案】A\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"bbox\": [\n" +
            "      741,\n" +
            "      2299,\n" +
            "      910,\n" +
            "      2334\n" +
            "    ],\n" +
            "    \"category\": \"Page-footer\",\n" +
            "    \"text\": \"第7页/共12页\"\n" +
            "  }\n" +
            "]\n";
}

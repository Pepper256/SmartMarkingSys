package use_case;

import java.nio.file.Paths;

public class Constants {

    public static final String API_MODEL = "qwen3-vl-flash";
    public static final String EXTRACT_STUDENT_API_MODEL = "qwen3-vl-plus";

    public static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    public static final String OCR_API_URL = "http://localhost:8000/v1/chat/completions";

    /**
     * SQLite database file path.
     *
     * NOTE: Do NOT put a writable database under src/main/resources.
     * Resources may become read-only after packaging.
     */
    public static final String SQLITE_DB_PATH = "./database/smartmark.db";

    public static final String OCR_MODEL_NAME = "rednote-hilab/dots.ocr";
    public static final String OCR_API_KEY = "random666ydbyl";
    public static final double DEFAULT_TEMP = 0.1;
    public static final double DEFAULT_TOP_P = 0.9;
    public static final int MAX_TOKENS = 32768;

    public static final String ANSWER_PROMPT = """
            你是一个高精度的 OCR 试卷识别助手。请分析图片并输出 JSON：
            {
              "subject": "学科",
              "questions": {"题号": "内容"},
              "answers": {"题号": "解析或答案"}
            }
            如果题目或答案包含图片，请将图片内容描述清楚并在位置标注 [IMAGE:<对应的base64字符串>]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增
            如果题目中，大题内出现小题，保存此小题为大题题号拼接小题题号的形式，不要出现嵌套json对象。
            假设一道题目为20（1）和20（2）出现小题时题号保存的示例：
            ...
            \\"20（1）\\":\\"相关内容\\",
            \\"20(2)\\":\\"相关内容\\"
            ...
            如果没有指明小题题号归属于哪一道大题，你需要自己推理出正确的大题题号。
            注意，该试卷未做答，试卷上没有学生的答案。对于选择题，需要把选项也纳入问题中。
            """;
    public static final String EXAM_PROMPT = """
            你是一个老师。请识别图中的试卷内容。输出JSON格式：
            {
              "subject": "学科名",
              "questions": {"题号": "题目内容"}
            }
            。若题目有图，请保留描述并在内容中包含[IMAGE:<对应的base64字符串>]。若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增。
            如果题目中，大题内出现小题，保存此小题为大题题号拼接小题题号的形式，不要出现嵌套json对象。
            假设一道题目的题号为20（1）和20（2），表示第20道大题的第1和第2小题，题号保存的示例：
            ...
            \\"20（1）\\":\\"相关内容\\",
            \\"20（2）\\":\\"相关内容\\"
            ...
            如果没有指明小题题号归属于哪一道大题，你需要自己推理出正确的大题题号。
            注意，该试卷未做答，试卷上没有学生的答案。对于选择题，需要把选项也纳入问题中。
            """;
    public static final String STUDENT_PROMPT = """
            # Role
           你是一个没有任何知识储备的“文本差异扫描仪”。你的唯一功能是：找出 [Student_Work_Markdown] 中比 [Blank_Template_JSON] 多出的物理字符。
           
           # Workflow (严格执行)
           1. **定位题干**：在 [Student_Work_Markdown] 中找到与 [Blank_Template_JSON] 文本描述完全一致的区域。
           2. **物理比对**：
              - 逐字比对两个版本的差异。
              - **如果差异为 0**（即学生版没写字，只有模板自带的括号、下划线、空格）：该题 responses 必须为 ""。
              - **如果差异 > 0**：仅提取多出来的字符。
           3. **禁止推理**：严禁根据你的理解去填写答案。哪怕题目是“1+1=”，只要学生版对应位置没写数字，你必须返回 ""。
           
           # Rules
           - **拼接题号**：大题号与小题号直接拼接，严禁分隔符（如 "19(1)"）。
           - **扁平结构**：JSON 禁止嵌套。
           - **输出约束**：仅输出纯 JSON，严禁任何解释语。
           
           # JSON Schema
           {
             "subject": "学科名",
             "responses": {
               "拼接题号": "提取的增量内容或空字符串"
             }
           }
           
           # Input Data
           [Blank_Template_JSON]
           {{blank_template}}
           
           [Student_Work_Markdown]
           {{student_work}}
            """;
    public static final String MARKING_PROMPT = """
                    你是一个老师。请识别图中试卷内容并批改试卷。输入json1，json2和json3
                    json1包含题号到题目和学生答案的嵌套键值对。json2为包含坐标信息的学生作答试卷。
                    json2是一个列表，包含坐标信息，你需要推理并批改对应学生作答的部分。
                    json3为题号和答案的键值对，供批改参考
                    
                    输出JSON格式，包含两个字段：
                    {
                      "answerInfo": {
                        "题号": {
                          "question": "题目内容",
                          "answer": "回答内容",
                          "correctness": "true或false，代表该题正确性。",
                          "reason": "如果该题正确，则值为空，如果该题错误，则分析错误原因并将分析出的错因作为该字段的值。"
                        }
                      },
                      "markWithCoords": json2，保持该json格式不变，在列表的每一项可能为学生回答的json对象内添加新的字段，"correctness": true或false，代表该题正确性。
                    }
                    
                    若有字段为空，留空字符串，如果没有题号，则题号由你生成从1开始递增
                    如果题目中，大题内出现小题，保存此小题为大题题号拼接小题题号的形式，不要出现嵌套json对象。
                            假设一道题目的题号为20（1）和20（2），表示第20道大题的第1和第2小题，题号保存的示例：
                            ...
                            \\"20（1）\\":<相关内容>,
                            \\"20（2）\\":<相关内容>
                            ...
                            如果没有指明小题题号归属于哪一道大题，你需要自己推理出正确的大题题号。
            """;

    public static final String REPORT_PROMPT = """
            你是一位专业的AI助教。请根据以下已批改试卷的数据及图片内容，生成一份详细的学习总结纸质报告。报告要求包含以下几部分

            【科目】：

            【学生答题详情】：

            【报告要求】：
            1. 分析学生在各个知识点上的掌握情况。
            2. 总结典型的错误类型（如：概念模糊、计算失误、审题不严）。
            3. 结合批改图片中的痕迹，评价学生的书写或解题过程。
            4. 提供不少于3条具体的后续学习建议。
            5. 整理易错点

            请直接输出报告正文，不要包含多余的开场白。在报告的结尾，不要输出\"报告完毕\"或相近意思的字，只需要以一条实线结尾即可
            """;

    public static final String OCR_PROMPT = """
    Please output the layout information from the PDF image, including each layout element's bbox, its category, and the corresponding text content within the bbox.
    
    1. Bbox format: [x1, y1, x2, y2]
    
    2. Layout Categories: The possible categories are ['Caption', 'Footnote', 'Formula', 'List-item', 'Page-footer', 'Page-header', 'Picture', 'Section-header', 'Table', 'Text', 'Title'].
    
    3. Text Extraction & Formatting Rules:
        - Picture: For the 'Picture' category, the text field should be omitted.
        - Formula: Format its text as LaTeX.
        - Table: Format its text as HTML.
        - All Others (Text, Title, etc.): Format their text as Markdown.
    
    4. Constraints:
        - The output text must be the original text from the image, with no translation.
        - All layout elements must be sorted according to human reading order.
    
    5. Final Output: The entire output must be a single JSON object.
    """;

    public static final String DOWNLOAD_PATH = Paths.get(System.getProperty("user.home"), "Downloads").toString();

    public static final String FONT_RESOURCE_PATH = "/fonts/simsun.ttf";
    public static final String FONT_FAMILY_NAME = "SimSun";

    // TODO: delete this when ocr is integrated
    public static final String TEST_OCR_RESPONSE = """
    [
      {
        "bbox": [493, 131, 1158, 286],
        "category": "Title",
        "text": "# 2023 北京朝阳初二(下)期末\\n## 道德与法治"
      },
      {
        "bbox": [147, 321, 1488, 414],
        "category": "Text",
        "text": "1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )"
      },
      {
        "bbox": [147, 436, 483, 472],
        "category": "Text",
        "text": "A. 首次实现空间交会对接"
      },
      {
        "bbox": [147, 495, 480, 530],
        "category": "Text",
        "text": "B. 首次多人多天载人飞行"
      },
      {
        "bbox": [147, 552, 615, 588],
        "category": "Text",
        "text": "C. 航天员乘组首次实现“太空会师”"
      },
      {
        "bbox": [147, 611, 745, 646],
        "category": "Text",
        "text": "D. 航天飞行工程师 and 载荷专家的首次太空飞行"
      },
      {
        "bbox": [147, 669, 1169, 704],
        "category": "Text",
        "text": "2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )"
      },
      {
        "bbox": [153, 728, 1312, 1120],
        "category": "Picture"
      },
      {
        "bbox": [147, 1145, 687, 1181],
        "category": "Text",
        "text": "A. 宪法规定实现公民基本权利的保障措施"
      },
      {
        "bbox": [147, 1204, 888, 1239],
        "category": "Text",
        "text": "B. 宪法与我们息息相关,我们的一生都离不开宪法的保护"
      },
      {
        "bbox": [147, 1261, 595, 1297],
        "category": "Text",
        "text": "C. 宪法规定国家生活中的根本问题"
      },
      {
        "bbox": [147, 1320, 656, 1355],
        "category": "Text",
        "text": "D. 一切权力属于人民是我国的宪法原则"
      },
      {
        "bbox": [147, 1378, 1198, 1413],
        "category": "Text",
        "text": "3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )"
      },
      {
        "bbox": [147, 1434, 616, 1468],
        "category": "Text",
        "text": "①彤彤上课认真听讲,按时完成作业"
      },
      {
        "bbox": [147, 1491, 644, 1526],
        "category": "Text",
        "text": "②哥哥将压岁钱存到银行,并获得利息"
      },
      {
        "bbox": [147, 1549, 879, 1584],
        "category": "Text",
        "text": "③爸爸向人大代表反映小区增设新能源汽车充电桩的问题"
      },
      {
        "bbox": [147, 1607, 703, 1641],
        "category": "Text",
        "text": "④妈妈参加了新一届朝阳区人大代表的选举"
      },
      {
        "bbox": [147, 1662, 249, 1697],
        "category": "Text",
        "text": "A. ①②"
      },
      {
        "bbox": [468, 1662, 567, 1697],
        "category": "Text",
        "text": "B. ①④"
      },
      {
        "bbox": [730, 1662, 830, 1697],
        "category": "Text",
        "text": "C. ②③"
      },
      {
        "bbox": [993, 1662, 1093, 1697],
        "category": "Text",
        "text": "D. ③④"
      },
      {
        "bbox": [147, 1720, 585, 1755],
        "category": "Text",
        "text": "4. 如图中①、②两处应填( )"
      },
      {
        "bbox": [153, 1780, 1022, 2082],
        "category": "Picture"
      },
      {
        "bbox": [741, 2299, 911, 2333],
        "category": "Page-footer",
        "text": "第1页/共12页"
      },
      {
        "bbox": [724, 131, 928, 185],
        "category": "Title",
        "text": "参考答案"
      },
      {
        "bbox": [149, 225, 315, 260],
        "category": "Text",
        "text": "1.【答案】D"
      },
      {
        "bbox": [149, 281, 1493, 487],
        "category": "Text",
        "text": "【解析】2023年5月30日,搭载神舟十六号载人飞船的长征二号F运载火箭...故D符合题意;ABC不符合题意。"
      },
      {
        "bbox": [149, 509, 272, 543],
        "category": "Text",
        "text": "故选：D。"
      },
      {
        "bbox": [149, 565, 805, 600],
        "category": "Text",
        "text": "本题考查时事政治。具体涉及神舟十六号载人飞船。"
      },
      {
        "bbox": [149, 622, 1126, 657],
        "category": "Text",
        "text": "解答该题要多关注时事,平时要注意养成关心国家大事的习惯,多了解时事。"
      },
      {
        "bbox": [149, 680, 311, 715],
        "category": "Text",
        "text": "2.【答案】B"
      },
      {
        "bbox": [149, 736, 1493, 885],
        "category": "Text",
        "text": "【解析】分析漫画可知,体现了宪法与我们息息相关...B是正确的选项;ACD不符合题意。"
      },
      {
        "bbox": [149, 906, 272, 941],
        "category": "Text",
        "text": "故选：B。"
      },
      {
        "bbox": [149, 964, 1449, 999],
        "category": "Text",
        "text": "本题考查了宪法是国家的根本法。宪法是国家的根本法，具有最高的法律效力，是治国安邦的总章程。"
      },
      {
        "bbox": [149, 1021, 1461, 1111],
        "category": "Text",
        "text": "本题要正确理解题意,只有理解题意...才能做出正确选择。"
      },
      {
        "bbox": [149, 1135, 315, 1170],
        "category": "Text",
        "text": "3.【答案】D"
      },
      {
        "bbox": [149, 1192, 1493, 1340],
        "category": "Text",
        "text": "【解析】题干中,爸爸向人大代表反映小区增设新能源汽车充电桩的问题...③④说法正确;①属于行使受教育权;②属于行使财产的收益权。"
      },
      {
        "bbox": [149, 1362, 272, 1397],
        "category": "Text",
        "text": "故选：D。"
      },
      {
        "bbox": [149, 1419, 1493, 1509],
        "category": "Text",
        "text": "本题考查正确行使政治权利。公民应当珍惜并正确行使民主政治权利..."
      },
      {
        "bbox": [149, 1531, 1331, 1566],
        "category": "Text",
        "text": "本题属基础知识题，考查正确行使政治权利，根据教材知识，依据题意分析，选出正确答案。"
      },
      {
        "bbox": [149, 1590, 315, 1624],
        "category": "Text",
        "text": "4.【答案】D"
      },
      {
        "bbox": [149, 1646, 1493, 1795],
        "category": "Text",
        "text": "【解析】我国是人民民主专政的国家...D是正确的选项；ABC描述都不正确。"
      },
      {
        "bbox": [149, 1816, 272, 1851],
        "category": "Text",
        "text": "故选：D。"
      },
      {
        "bbox": [149, 1873, 1493, 1964],
        "category": "Text",
        "text": "本题考查了人民代表大会制度。"
      },
      {
        "bbox": [149, 1985, 1449, 2020],
        "category": "Text",
        "text": "解答本题需明确考查的知识点是人民代表大会制度，在此基础上，结合分析各个说法，选出正确答案。"
      },
      {
        "bbox": [149, 2044, 315, 2079],
        "category": "Text",
        "text": "5.【答案】A"
      },
      {
        "bbox": [741, 2299, 910, 2334],
        "category": "Page-footer",
        "text": "第7页/共12页"
      }
    ]
    """;

    public static final String TEST_STUDENT_OCR_RESPONSE = """
            [
              {
                "bbox": [493, 131, 1158, 286],
                "category": "Title",
                "text": "# 2023 北京朝阳初二(下)期末\\n## 道德与法治"
              },
              {
                "bbox": [147, 321, 1488, 414],
                "category": "Text",
                "text": "1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国(A)"
              },
              {
                "bbox": [147, 436, 483, 472],
                "category": "Text",
                "text": "A. 首次实现空间交会对接"
              },
              {
                "bbox": [147, 495, 480, 530],
                "category": "Text",
                "text": "B. 首次多人多天载人飞行"
              },
              {
                "bbox": [147, 552, 615, 588],
                "category": "Text",
                "text": "C. 航天员乘组首次实现“太空会师”"
              },
              {
                "bbox": [147, 611, 745, 646],
                "category": "Text",
                "text": "D. 航天飞行工程师 and 载荷专家的首次太空飞行"
              },
              {
                "bbox": [147, 669, 1169, 704],
                "category": "Text",
                "text": "2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是(B)"
              },
              {
                "bbox": [153, 728, 1312, 1120],
                "category": "Picture"
              },
              {
                "bbox": [147, 1145, 687, 1181],
                "category": "Text",
                "text": "A. 宪法规定实现公民基本权利的保障措施"
              },
              {
                "bbox": [147, 1204, 888, 1239],
                "category": "Text",
                "text": "B. 宪法与我们息息相关,我们的一生都离不开宪法的保护"
              },
              {
                "bbox": [147, 1261, 595, 1297],
                "category": "Text",
                "text": "C. 宪法规定国家生活中的根本问题"
              },
              {
                "bbox": [147, 1320, 656, 1355],
                "category": "Text",
                "text": "D. 一切权力属于人民是我国的宪法原则"
              },
              {
                "bbox": [147, 1378, 1198, 1413],
                "category": "Text",
                "text": "3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有(C)"
              },
              {
                "bbox": [147, 1434, 616, 1468],
                "category": "Text",
                "text": "①彤彤上课认真听讲,按时完成作业"
              },
              {
                "bbox": [147, 1491, 644, 1526],
                "category": "Text",
                "text": "②哥哥将压岁钱存到银行,并获得利息"
              },
              {
                "bbox": [147, 1549, 879, 1584],
                "category": "Text",
                "text": "③爸爸向人大代表反映小区增设新能源汽车充电桩的问题"
              },
              {
                "bbox": [147, 1607, 703, 1641],
                "category": "Text",
                "text": "④妈妈参加了新一届朝阳区人大代表的选举"
              },
              {
                "bbox": [147, 1662, 249, 1697],
                "category": "Text",
                "text": "A. ①②"
              },
              {
                "bbox": [468, 1662, 567, 1697],
                "category": "Text",
                "text": "B. ①④"
              },
              {
                "bbox": [730, 1662, 830, 1697],
                "category": "Text",
                "text": "C. ②③"
              },
              {
                "bbox": [993, 1662, 1093, 1697],
                "category": "Text",
                "text": "D. ③④"
              },
              {
                "bbox": [147, 1720, 585, 1755],
                "category": "Text",
                "text": "4. 如图中①、②两处应填(D)"
              },
              {
                "bbox": [153, 1780, 1022, 2082],
                "category": "Picture"
              },
              {
                "bbox": [741, 2299, 911, 2333],
                "category": "Page-footer",
                "text": "第1页/共12页"
              }
            ]
            """;
}

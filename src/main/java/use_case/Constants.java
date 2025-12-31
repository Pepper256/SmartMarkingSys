package use_case;

import java.nio.file.Paths;

public class Constants {

    public static final String QWEN_API_MODEL = "qwen3-vl-flash";
    public static final String DEEPSEEK_API_MODEL = "deepseek-v3.2";

    public static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    public static final String DEEPSEEK_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
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
            你是一个极度死板、无意识的“答题卡填涂状态分析专家”。你的任务是精准分辨 OCR 文本中的“标准题号”、“填涂内容”与“作答文字”，并严格过滤掉“分值”和“得分”等干扰信息。
            
            # Workflow (严格执行)
            
            1. **题号定位与去噪**:
               - 定位数字开头的题号（如 "1.", "19."）。
               - **剔除分值/得分**：若题号旁出现类似 `(5分)`, `[10分]`, `得分：`, `Score:`, `评卷人` 或孤立的两位数（阅卷得分），必须将其视作干扰项彻底忽略，严禁将其作为答案。
            
            2. **选择题填涂状态判定 (核心逻辑)**:
                  - **选项容器识别**：`[ A ]`, `( A )`, `[A]`, `(A)` 均被视为**原始空白选项框**，不代表填涂。
                  - **空选判定 (Strict Negative)**：
                    - 若选项表现为 `[ A ] B C D`（内部仅有字母和空格），Value 必须填 `""`。
                    - **禁止**将方括号 `[]` 或圆括号 `()` 本身视为填涂标记。
                  - **填涂判定 (Positive)**：
                    - **内部填充**：只有当括号/方框内部除了字母，还挤入了明确的标记字符（如 `[x]`, `[√]`, `[■]`, `[*]`, `[v]`, `[.]`），对应的字母才是答案。
                    - **字母覆盖**：如果原本应该是 A 的位置字母消失，变成了填充符（如 `[x] B C D`），则答案是 A。
                    - **标记优于容器**：如果文本是 `[ A ] [x] [ C ] [ D ]`，只有第 2 个位置是填涂，提取答案为 "B"。
            
            3. **主观题/填空题提取**:
               - 提取从当前题干结束到下一个题号出现前的所有文字。
               - **内容清洗**：如果提取的内容中包含 `得分：` 或教师批改的痕迹字符，必须将其剔除，仅保留学生书写的原文。
            
            # Rules
            - **禁止知识补全**：你没有任何常识。如果学生没答，绝对不能填入任何内容。
            - **全量提取**：按 OCR 文本出现的题号顺序生成 JSON，不遗漏任何题号。
            - **直接拼接题号**：大题号与小题号直接拼接（例："1(1)", "20"）。
            - **输出约束**：仅输出纯 JSON 字符串，禁止包含 Markdown 代码块。
            
            # JSON Schema
            {
              "subject": "学科名",
              "responses": {
                "拼接题号": "提取的答案或空字符串"
              }
            }
            
            # Input Data
            [Student_Card_OCR]
            {{student_card_ocr}}
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
            你是一位专业的AI助教。请根据以下已批改试卷的数据内容，生成一份详细的学习总结纸质报告。报告要求包含以下几部分

            【科目】：

            【学生答题详情】：

            【报告要求】：
            1. 分析学生在各个知识点上的掌握情况。
            2. 总结典型的错误类型（如：概念模糊、计算失误、审题不严）。
            3. 提供不少于3条具体的后续学习建议。
            4. 整理易错点

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
                 "bbox": [
                   269,
                   73,
                   439,
                   95
                 ],
                 "category": "Title",
                 "text": "道德与法治期末测试"
               },
               {
                 "bbox": [
                   47,
                   141,
                   157,
                   161
                 ],
                 "category": "Text",
                 "text": "考号：________"
               },
               {
                 "bbox": [
                   169,
                   141,
                   277,
                   161
                 ],
                 "category": "Text",
                 "text": "学校：________"
               },
               {
                 "bbox": [
                   289,
                   141,
                   397,
                   161
                 ],
                 "category": "Text",
                 "text": "姓名：________"
               },
               {
                 "bbox": [
                   409,
                   141,
                   507,
                   161
                 ],
                 "category": "Text",
                 "text": "班级：________"
               },
               {
                 "bbox": [
                   519,
                   141,
                   617,
                   161
                 ],
                 "category": "Text",
                 "text": "座号：________"
               },
               {
                 "bbox": [
                   210,
                   175,
                   277,
                   193
                 ],
                 "category": "Section-header",
                 "text": "注意事项"
               },
               {
                 "bbox": [
                   55,
                   211,
                   328,
                   225
                 ],
                 "category": "Text",
                 "text": "1. 答题前请将姓名、班级、考场、座号和准考证号填写清楚。"
               },
               {
                 "bbox": [
                   55,
                   232,
                   314,
                   246
                 ],
                 "category": "Text",
                 "text": "2. 客观题答题，必须使用2B铅笔填涂，修改时用橡皮擦干净。"
               },
               {
                 "bbox": [
                   55,
                   250,
                   218,
                   263
                 ],
                 "category": "Text",
                 "text": "3. 主观题必须使用黑色签字笔书写。"
               },
               {
                 "bbox": [
                   55,
                   270,
                   330,
                   283
                 ],
                 "category": "Text",
                 "text": "4. 必须在题号对应的答题区域内作答，超出答题区域书写无效。"
               },
               {
                 "bbox": [
                   55,
                   289,
                   158,
                   303
                 ],
                 "category": "Text",
                 "text": "5. 保持答卷清洁完整。"
               },
               {
                 "bbox": [
                   507,
                   255,
                   586,
                   274
                 ],
                 "category": "Text",
                 "text": "贴条形码区"
               },
               {
                 "bbox": [
                   53,
                   328,
                   110,
                   346
                 ],
                 "category": "Text",
                 "text": "正确填涂"
               },
               {
                 "bbox": [
                   140,
                   330,
                   158,
                   345
                 ],
                 "category": "Picture"
               },
               {
                 "bbox": [
                   185,
                   328,
                   242,
                   346
                 ],
                 "category": "Text",
                 "text": "缺考标记"
               },
               {
                 "bbox": [
                   295,
                   330,
                   313,
                   345
                 ],
                 "category": "Picture"
               },
               {
                 "bbox": [
                   49,
                   367,
                   132,
                   382
                 ],
                 "category": "Section-header",
                 "text": "一、选择题(60分)"
               },
               {
                 "bbox": [
                   63,
                   392,
                   378,
                   407
                 ],
                 "category": "Text",
                 "text": "1. [x] B C D 6. [A] B C D 11. [A] B C D"
               },
               {
                 "bbox": [
                   62,
                   409,
                   378,
                   421
                 ],
                 "category": "Text",
                 "text": "2. [A] B C D 7. [A] B C D 12. [A] B C D"
               },
               {
                 "bbox": [
                   62,
                   424,
                   378,
                   437
                 ],
                 "category": "Text",
                 "text": "3. [A] B C D 8. [A] B C D 13. [A] B C D"
               },
               {
                 "bbox": [
                   62,
                   440,
                   378,
                   453
                 ],
                 "category": "Text",
                 "text": "4. [A] B C D 9. [A] B C D 14. [A] B C D"
               },
               {
                 "bbox": [
                   62,
                   456,
                   378,
                   469
                 ],
                 "category": "Text",
                 "text": "5. [A] B C D 10. [A] B C D 15. [A] B C D"
               },
               {
                 "bbox": [
                   49,
                   499,
                   126,
                   514
                 ],
                 "category": "Section-header",
                 "text": "二、填空题(8分)"
               },
               {
                 "bbox": [
                   61,
                   519,
                   141,
                   545
                 ],
                 "category": "Text",
                 "text": "16(1) 666"
               },
               {
                 "bbox": [
                   202,
                   519,
                   284,
                   547
                 ],
                 "category": "Text",
                 "text": "(2) 不会"
               },
               {
                 "bbox": [
                   49,
                   561,
                   101,
                   575
                 ],
                 "category": "Section-header",
                 "text": "三、解答题"
               },
               {
                 "bbox": [
                   61,
                   579,
                   222,
                   605
                 ],
                 "category": "Text",
                 "text": "17(6分) 取长补短"
               },
               {
                 "bbox": [
                   61,
                   739,
                   98,
                   752
                 ],
                 "category": "Text",
                 "text": "18(7分)"
               },
               {
                 "bbox": [
                   696,
                   112,
                   741,
                   126
                 ],
                 "category": "Text",
                 "text": "19.1(4分)"
               },
               {
                 "bbox": [
                   696,
                   261,
                   741,
                   275
                 ],
                 "category": "Text",
                 "text": "19.2(5分)"
               },
               {
                 "bbox": [
                   696,
                   411,
                   741,
                   424
                 ],
                 "category": "Text",
                 "text": "20.1(2分)"
               },
               {
                 "bbox": [
                   696,
                   559,
                   741,
                   573
                 ],
                 "category": "Text",
                 "text": "20.2(3分)"
               },
               {
                 "bbox": [
                   696,
                   708,
                   741,
                   722
                 ],
                 "category": "Text",
                 "text": "20.3(5分)"
               },
               {
                 "bbox": [
                   205,
                   892,
                   266,
                   909
                 ],
                 "category": "Page-footer",
                 "text": "@鲁青信息"
               },
               {
                 "bbox": [
                   627,
                   892,
                   701,
                   909
                 ],
                 "category": "Page-footer",
                 "text": "第1页共1页"
               }
             ]
            """;
}

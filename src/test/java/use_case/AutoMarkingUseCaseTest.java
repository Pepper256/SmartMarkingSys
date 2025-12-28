package use_case;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entities.AnswerPaper;
import entities.MarkedStudentPaper;
import entities.StudentPaper;
import interface_adapter.auto_marking.AutoMarkingPresenter;
import use_case.auto_marking.AutoMarkingDataAccessInterface;
import use_case.auto_marking.AutoMarkingUseCase;
import use_case.dto.AutoMarkingInputData;

public class AutoMarkingUseCaseTest {

    private AutoMarkingUseCase useCase;
    private final AutoMarkingPresenter presenter = new AutoMarkingPresenter();
    private final MockDao dao = new MockDao();
    private final List<String> ids = new ArrayList<>();

    @BeforeEach
    void setup() {
        useCase = new AutoMarkingUseCase(presenter, dao);

        ids.add("1");
    }

    @Test
    public void AutoMarkingUseCaseApiTest() {
        useCase.execute(new AutoMarkingInputData(ids));
    }

    private static class MockDao implements AutoMarkingDataAccessInterface {

        @Override
        public StudentPaper getStudentPaperById(String id) {
            HashMap<String, String> questions = getQuestions();

            HashMap<String, String> responses = new HashMap<>();
            responses.put("1", "D");
            responses.put("2", "B");
            responses.put("3", "C");
            responses.put("4", "D");

            return new StudentPaper(
                    "1",
                    "1",
                    "1",
                    questions,
                    responses,
                    "[\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      492,\n" +
                            "      132,\n" +
                            "      1157,\n" +
                            "      283\n" +
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
                            "    \"text\": \"1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国(D)\"\n" +
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
                            "      483,\n" +
                            "      531\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"B. 首次多人多天载人飞行\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      553,\n" +
                            "      615,\n" +
                            "      589\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"C. 航天员乘组首次实现“太空会师”\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      612,\n" +
                            "      745,\n" +
                            "      648\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"D. 航天飞行工程师和载荷专家的首次太空飞行\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      669,\n" +
                            "      1170,\n" +
                            "      705\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是(B)\"\n" +
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
                            "      688,\n" +
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
                            "      1240\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"B. 宪法与我们息息相关,我们的一生都离不开宪法的保护\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1262,\n" +
                            "      596,\n" +
                            "      1298\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"C. 宪法规定国家生活中的根本问题\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1321,\n" +
                            "      656,\n" +
                            "      1357\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"D. 一切权力属于人民是我国的宪法原则\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1378,\n" +
                            "      1198,\n" +
                            "      1414\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有(C)\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1434,\n" +
                            "      617,\n" +
                            "      1469\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"①彤彤上课认真听讲,按时完成作业\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1490,\n" +
                            "      646,\n" +
                            "      1526\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"②哥哥将压岁钱存到银行,并获得利息\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1547,\n" +
                            "      879,\n" +
                            "      1583\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1604,\n" +
                            "      704,\n" +
                            "      1639\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"④妈妈参加了新一届朝阳区人大代表的选举\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1662,\n" +
                            "      250,\n" +
                            "      1698\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"A. ①②\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      468,\n" +
                            "      1662,\n" +
                            "      569,\n" +
                            "      1698\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"B. ①④\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      730,\n" +
                            "      1662,\n" +
                            "      831,\n" +
                            "      1698\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"C. ②③\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      993,\n" +
                            "      1662,\n" +
                            "      1094,\n" +
                            "      1698\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"D. ③④\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      147,\n" +
                            "      1721,\n" +
                            "      586,\n" +
                            "      1757\n" +
                            "    ],\n" +
                            "    \"category\": \"Text\",\n" +
                            "    \"text\": \"4. 如图中①、②两处应填(D)\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"bbox\": [\n" +
                            "      153,\n" +
                            "      1781,\n" +
                            "      1022,\n" +
                            "      2081\n" +
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
                            "  }\n" +
                            "]\n"
            );
        }

        private @NotNull HashMap<String, String> getQuestions() {
            HashMap<String, String> questions = new HashMap<>();
            questions.put("1", "北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )A. 首次实现空间交会对接\n" +
                    "\n" +
                    "B. 首次多人多天载人飞行\n" +
                    "\n" +
                    "C. 航天员乘组首次实现“太空会师”\n" +
                    "\n" +
                    "D. 航天飞行工程师和载荷专家的首次太空飞行");
            questions.put("2", "八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )\n" +
                    "A. 宪法规定实现公民基本权利的保障措施\n" +
                    "\n" +
                    "B. 宪法与我们息息相关,我们的一生都离不开宪法的保护\n" +
                    "\n" +
                    "C. 宪法规定国家生活中的根本问题\n" +
                    "\n" +
                    "D. 一切权力属于人民是我国的宪法原则");
            questions.put("3", "下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )\n" +
                    "①彤彤上课认真听讲,按时完成作业\n" +
                    "\n" +
                    "②哥哥将压岁钱存到银行,并获得利息\n" +
                    "\n" +
                    "③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\n" +
                    "\n" +
                    "④妈妈参加了新一届朝阳区人大代表的选举\n" +
                    "\n" +
                    "A. ①②\n" +
                    "\n" +
                    "B. ①④\n" +
                    "\n" +
                    "C. ②③\n" +
                    "\n" +
                    "D. ③④");
            questions.put("4", "如图中①、②两处应填( )\n" +
                    "A. 人大代表、人民代表大会 B. 人民、人大代表 C. 人民、全国人民代表大会 D. 人民、人民代表大会");
            return questions;
        }

        @Override
        public void storeMarkedPapers(List<MarkedStudentPaper> studentPapers) {
            System.out.println(studentPapers.get(0).toJsonString());
            System.out.println("storing success");
        }

        @Override
        public AnswerPaper getAnswerPaperByExamPaperId(String examPaperId) {
            HashMap<String, String> answers = new HashMap<>();
            answers.put("1", "D");
            answers.put("2", "B");
            answers.put("3", "D");
            answers.put("4", "D");

            return new AnswerPaper(
                    "1",
                    "1",
                    "1",
                    getQuestions(),
                    answers
            );
        }
    }
}

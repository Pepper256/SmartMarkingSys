package use_case;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entities.MarkedStudentPaper;
import entities.Report;
import interface_adapter.generate_student_report.GenerateStudentReportPresenter;
import use_case.dto.GenerateStudentReportInputData;
import use_case.generate_student_report.GenerateStudentReportDataAccessInterface;
import use_case.generate_student_report.GenerateStudentReportUseCase;

public class GenerateStudentReportUseCaseTest {

    private final GenerateStudentReportPresenter presenter = new GenerateStudentReportPresenter();
    private final MockDao dao = new MockDao();
    private GenerateStudentReportUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new GenerateStudentReportUseCase(presenter, dao);
    }

    @Test
    public void GenerateStudentReportUseCaseApiTest() {
        useCase.execute(new GenerateStudentReportInputData("1"));
    }

    private static class MockDao implements GenerateStudentReportDataAccessInterface {


        @Override
        public void storeReport(Report report) {
            System.out.println(report.toJsonString());
            System.out.println("storing success");
        }

        @Override
        public MarkedStudentPaper getMarkedStudentPaperById(String markedStudentPaperId) {
            return entities.MarkedStudentPaper.jsonToMarkedStudentPaper("""
    {
      "correctness": {
        "1": true,
        "2": false,
        "3": true,
        "4": true
      },
      "reasons": {
        "1": "",
        "2": "学生答案为C，但根据参考答案，正确答案应为B。宪法的核心作用是保障公民权利和规范国家权力，虽然C选项描述也正确（宪法规定国家根本问题），但B选项更贴合‘小红对照宪法内容梳理’语境中强调宪法与个人生活的关联性，且在初中道德与法治教学中，常将‘宪法与我们息息相关’作为重点认知点。因此，学生选C虽非错误，但不符合本题考查意图，应判为错误。",
        "3": "",
        "4": ""
      },
      "markedContent": "[{\\"correctness\\":\\"\\",\\"bbox\\":[492,132,1157,283],\\"text\\":\\"# 2023 北京朝阳初二(下)期末\\\\n## 道德与法治\\",\\"category\\":\\"Title\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,321,1488,414],\\"text\\":\\"1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,436,483,472],\\"text\\":\\"A. 首次实现空间交会对接\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,495,483,531],\\"text\\":\\"B. 首次多人多天载人飞行\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,553,615,589],\\"text\\":\\"C. 航天员乘组首次实现“太空会师”\\",\\"category\\":\\"Text\\"},{\\"correctness\\":true,\\"bbox\\":[147,612,745,648],\\"text\\":\\"D. 航天飞行工程师和载荷专家的首次太空飞行\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,669,1170,705],\\"text\\":\\"2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[153,728,1312,1120],\\"category\\":\\"Picture\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1145,688,1181],\\"text\\":\\"A. 宪法规定实现公民基本权利的保障措施\\",\\"category\\":\\"Text\\"},{\\"correctness\\":false,\\"bbox\\":[147,1204,888,1240],\\"text\\":\\"B. 宪法与我们息息相关,我们的一生都离不开宪法的保护\\",\\"category\\":\\"Text\\"},{\\"correctness\\":false,\\"bbox\\":[147,1262,596,1298],\\"text\\":\\"C. 宪法规定国家生活中的根本问题\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1321,656,1357],\\"text\\":\\"D. 一切权力属于人民是我国的宪法原则\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1378,1198,1414],\\"text\\":\\"3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1434,617,1469],\\"text\\":\\"①彤彤上课认真听讲,按时完成作业\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1490,646,1526],\\"text\\":\\"②哥哥将压岁钱存到银行,并获得利息\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1547,879,1583],\\"text\\":\\"③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1604,704,1639],\\"text\\":\\"④妈妈参加了新一届朝阳区人大代表的选举\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1662,250,1698],\\"text\\":\\"A. ①②\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[468,1662,569,1698],\\"text\\":\\"B. ①④\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[730,1662,831,1698],\\"text\\":\\"C. ②③\\",\\"category\\":\\"Text\\"},{\\"correctness\\":true,\\"bbox\\":[993,1662,1094,1698],\\"text\\":\\"D. ③④\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[147,1721,586,1757],\\"text\\":\\"4. 如图中①、②两处应填( )\\",\\"category\\":\\"Text\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[153,1781,1022,2081],\\"category\\":\\"Picture\\"},{\\"correctness\\":\\"\\",\\"bbox\\":[741,2299,911,2333],\\"text\\":\\"第1页/共12页\\",\\"category\\":\\"Page-footer\\"}]",
      "subject": "道德与法治",
      "coordContent": "[\\n  {\\n    \\"bbox\\": [\\n      492,\\n      132,\\n      1157,\\n      283\\n    ],\\n    \\"category\\": \\"Title\\",\\n    \\"text\\": \\"# 2023 北京朝阳初二(下)期末\\\\\\\\n## 道德与法治\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      321,\\n      1488,\\n      414\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"1. 北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      436,\\n      483,\\n      472\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"A. 首次实现空间交会对接\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      495,\\n      483,\\n      531\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"B. 首次多人多天载人飞行\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      553,\\n      615,\\n      589\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"C. 航天员乘组首次实现“太空会师”\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      612,\\n      745,\\n      648\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"D. 航天飞行工程师和载荷专家的首次太空飞行\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      669,\\n      1170,\\n      705\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"2. 八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      153,\\n      728,\\n      1312,\\n      1120\\n    ],\\n    \\"category\\": \\"Picture\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1145,\\n      688,\\n      1181\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"A. 宪法规定实现公民基本权利的保障措施\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1204,\\n      888,\\n      1240\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"B. 宪法与我们息息相关,我们的一生都离不开宪法的保护\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1262,\\n      596,\\n      1298\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"C. 宪法规定国家生活中的根本问题\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1321,\\n      656,\\n      1357\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"D. 一切权力属于人民是我国的宪法原则\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1378,\\n      1198,\\n      1414\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"3. 下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1434,\\n      617,\\n      1469\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"①彤彤上课认真听讲,按时完成作业\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1490,\\n      646,\\n      1526\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"②哥哥将压岁钱存到银行,并获得利息\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1547,\\n      879,\\n      1583\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1604,\\n      704,\\n      1639\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"④妈妈参加了新一届朝阳区人大代表的选举\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1662,\\n      250,\\n      1698\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"A. ①②\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      468,\\n      1662,\\n      569,\\n      1698\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"B. ①④\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      730,\\n      1662,\\n      831,\\n      1698\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"C. ②③\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      993,\\n      1662,\\n      1094,\\n      1698\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"D. ③④\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      147,\\n      1721,\\n      586,\\n      1757\\n    ],\\n    \\"category\\": \\"Text\\",\\n    \\"text\\": \\"4. 如图中①、②两处应填( )\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      153,\\n      1781,\\n      1022,\\n      2081\\n    ],\\n    \\"category\\": \\"Picture\\"\\n  },\\n  {\\n    \\"bbox\\": [\\n      741,\\n      2299,\\n      911,\\n      2333\\n    ],\\n    \\"category\\": \\"Page-footer\\",\\n    \\"text\\": \\"第1页/共12页\\"\\n  }\\n]",
      "examPaperId": "1",
      "questions": {
        "1": "北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( )A. 首次实现空间交会对接\\n\\nB. 首次多人多天载人飞行\\n\\nC. 航天员乘组首次实现“太空会师”\\n\\nD. 航天飞行工程师和载荷专家的首次太空飞行",
        "2": "八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( )\\nA. 宪法规定实现公民基本权利的保障措施\\n\\nB. 宪法与我们息息相关,我们的一生都离不开宪法的保护\\n\\nC. 宪法规定国家生活中的根本问题\\n\\nD. 一切权力属于人民是我国的宪法原则",
        "3": "下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( )\\n①彤彤上课认真听讲,按时完成作业\\n\\n②哥哥将压岁钱存到银行,并获得利息\\n\\n③爸爸向人大代表反映小区增设新能源汽车充电桩的问题\\n\\n④妈妈参加了新一届朝阳区人大代表的选举\\n\\nA. ①②\\n\\nB. ①④\\n\\nC. ②③\\n\\nD. ③④",
        "4": "如图中①、②两处应填( )\\nA. 人大代表、人民代表大会 B. 人民、人大代表 C. 人民、全国人民代表大会 D. 人民、人民代表大会"
      },
      "responses": {
        "1": "D",
        "2": "C",
        "3": "D",
        "4": "D"
      },
      "id": "1"
    }
    """);
        }
    }
}

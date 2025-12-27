package use_case;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entities.Report;
import interface_adapter.export_report.ExportReportPresenter;
import use_case.dto.ExportReportInputData;
import use_case.export_report.ExportReportDataAccessInterface;
import use_case.export_report.ExportReportUseCase;

public class ExportReportUseCaseTest {

    private ExportReportUseCase useCase;
    private final ExportReportPresenter presenter = new ExportReportPresenter();
    private final MockDao dao = new MockDao();

    @BeforeEach
    void setup() {
        useCase = new ExportReportUseCase(presenter, dao);
    }

    @Test
    public void ExportReportUseCasePdfTest() {
        useCase.execute(new ExportReportInputData("1"));
    }

    private static class MockDao implements ExportReportDataAccessInterface {

        @Override
        public Report getReportByReportId(String reportId) {
            return new Report(
                    "1",
                    "1",
                    "1",
                    "# 2025年春季学期第二次阶段性测评报告\n" +
                            "\n" +
                            "**学生姓名：** aaa  \n" +
                            "**班级：** 高二（3）班  \n" +
                            "**考试日期：** 2025年12月20日  \n" +
                            "**报告生成日期：** 2025年12月26日\n" +
                            "\n" +
                            "---\n" +
                            "\n" +
                            "## 一、 成绩总览\n" +
                            "\n" +
                            "| 科目 | 满分 | 个人得分 | 班级均分 | 排名变动 |\n" +
                            "| :--- | :--- | :--- | :--- | :--- |\n" +
                            "| **语文** | 150 | 122 | 108 | ↑ 3 |\n" +
                            "| **数学** | 150 | 145 | 115 | ↔ 持平 |\n" +
                            "| **英语** | 150 | 138 | 120 | ↑ 5 |\n" +
                            "| **物理** | 100 | 92 | 78 | ↑ 2 |\n" +
                            "| **化学** | 100 | 85 | 82 | ↓ 4 |\n" +
                            "| **总分** | **650** | **582** | **503** | **前 5%** |\n" +
                            "\n" +
                            "---\n" +
                            "\n" +
                            "## 二、 学科优劣势分析\n" +
                            "\n" +
                            "###  优势学科：数学 & 物理\n" +
                            "* **数学**：本次考试在“解析几何”与“概率统计”大题中拿到了满分。展现了极强的逻辑推理能力和计算准确度。\n" +
                            "* **物理**：实验设计题表现突出，能够灵活运用物理定律解决实际场景问题。\n" +
                            "\n" +
                            "###  需关注学科：化学\n" +
                            "* **短板**：本次化学考试中，“有机化学推断”部分失分较多。主要原因是对官能团转换的反应条件掌握不够扎实，存在知识盲区。\n" +
                            "\n" +
                            "---\n" +
                            "\n" +
                            "## 三、 知识点掌握能力雷达\n" +
                            "\n" +
                            "* **基础知识（记忆/理解）**：92%\n" +
                            "* **逻辑推理（分析/综合）**：95%\n" +
                            "* **应用实践（计算/实验）**：88%\n" +
                            "* **语言表达（作文/简答）**：82%\n" +
                            "\n" +
                            "---\n" +
                            "\n" +
                            "## 四、 教师综合评价及建议\n" +
                            "\n" +
                            "> **评价**：a同学本次表现非常出色，尤其是理科思维的严密性得到了阅卷老师的一致认可。但在文科主观题和理科规范化表达（如化学方程式的沉淀符号、物理过程"
            );
        }
    }
}

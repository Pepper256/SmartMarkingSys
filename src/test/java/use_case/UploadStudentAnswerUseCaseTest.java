package use_case;

import java.util.ArrayList;
import java.util.List;

import entities.ExamPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entities.StudentPaper;
import interface_adapter.upload_student_answer.UploadStudentAnswerPresenter;
import use_case.dto.UploadStudentAnswerInputData;
import use_case.upload_student_answer.UploadStudentAnswerDataAccessInterface;
import use_case.upload_student_answer.UploadStudentAnswerUseCase;

public class UploadStudentAnswerUseCaseTest {

    private final UploadStudentAnswerUseCase inputBoundary = new UploadStudentAnswerUseCase(
            new MockDao(),
            new UploadStudentAnswerPresenter()
    );

    private final List<String> paths = new ArrayList<>();

    @BeforeEach
    void setup() {
        paths.add("./src/main/resources/test_student_removed.pdf");
    }

    @Test
    public void UploadStudentAnswerUseCaseApiTest() {
        inputBoundary.execute(new UploadStudentAnswerInputData(paths, "1"));
    }

    private static class MockDao implements UploadStudentAnswerDataAccessInterface {

        @Override
        public void saveStudentPapers(List<StudentPaper> studentPapers) {
            System.out.println(studentPapers.get(0).toJsonString());
        }

        @Override
        public ExamPaper getExamPaperById(String examPaperId) {
            return ExamPaper.jsonToExamPaper("""
                    {
              "answerId": "ANSWER_1766958027813",
              "subject": "道德与法治",
              "questions": {
                "1": "北京时间2023年5月30日,神舟十六号载人飞船发射取得圆满成功。航天员乘组由景海鹏、朱杨柱、桂海潮3名航天员组成,这是我国( ) A. 首次实现空间交会对接 B. 首次多人多天载人飞行 C. 航天员乘组首次实现“太空会师” D. 航天飞行工程师 and 载荷专家的首次太空飞行",
                "2": "八年级学生小红对照宪法内容梳理出如图。对此,以下理解正确的是( ) A. 宪法规定实现公民基本权利的保障措施 B. 宪法与我们息息相关,我们的一生都离不开宪法的保护 C. 宪法规定国家生活中的根本问题 D. 一切权力属于人民是我国的宪法原则",
                "3": "下面初中生彤彤一家人的行为,能够体现依法行使政治权利和自由的有( ) ①彤彤上课认真听讲,按时完成作业 ②哥哥将压岁钱存到银行,并获得利息 ③爸爸向人大代表反映小区增设新能源汽车充电桩的问题 ④妈妈参加了新一届朝阳区人大代表的选举 A. ①② B. ①④ C. ②③ D. ③④",
                "4": "如图中①、②两处应填( )"
              },
              "id": "EXAM_9ccb415b"
            }""");
        }
    }
}

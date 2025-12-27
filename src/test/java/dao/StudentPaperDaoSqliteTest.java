package dao;

import entities.StudentPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StudentPaperDaoSqliteTest {

    private StudentPaperDao dao;

    @BeforeEach
    void reset() {
        DaoTestUtil.resetSchemaAndClearAllTables();
        dao = new StudentPaperDao();
    }

    @Test
    void saveThenLoadById_roundTrip() {
        HashMap<String, String> qs = new HashMap<String, String>();
        qs.put("1", "1+1=?");
        qs.put("2", "2+2=?");

        HashMap<String, String> rsp = new HashMap<String, String>();
        rsp.put("1", "2");
        rsp.put("2", "4");

        StudentPaper sp = new StudentPaper(
                "SP_001",
                "EXAM_001",
                "Math",
                qs,
                rsp,
                "coord-content"
        );

        List<StudentPaper> list = new ArrayList<StudentPaper>();
        list.add(sp);

        dao.saveStudentPapers(list);

        StudentPaper loaded = dao.getStudentPaperById("SP_001");
        assertNotNull(loaded);
        assertEquals(sp, loaded);
    }

    @Test
    void getAnswerPaperByExamPaperId_returnsLatest() {
        // Insert answer papers via ExamAnswerInfoDao, then query through StudentPaperDao.
        ExamAnswerInfoDao examDao = new ExamAnswerInfoDao();

        HashMap<String, String> qs = new HashMap<String, String>();
        qs.put("1", "Q1");

        HashMap<String, String> ans = new HashMap<String, String>();
        ans.put("1", "A1");

        entities.ExamPaper exam = new entities.ExamPaper("EXAM_002", "Math", qs, "ANS_002");
        entities.AnswerPaper answer = new entities.AnswerPaper("ANS_002", "EXAM_002", "Math", qs, ans);

        examDao.storeExamAnswer(exam, answer);

        entities.AnswerPaper loaded = dao.getAnswerPaperByExamPaperId("EXAM_002");
        assertNotNull(loaded);
        assertEquals(answer, loaded);
    }
}

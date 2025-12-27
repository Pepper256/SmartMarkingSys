package dao;

import entities.AnswerPaper;
import entities.ExamPaper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ExamAnswerInfoDaoSqliteTest {

    @Test
    void storeAndLoad_roundTrip(@TempDir Path tempDir) {
        String dbPath = tempDir.resolve("smartmark-test.db").toString();
        ExamAnswerInfoDao dao = new ExamAnswerInfoDao(dbPath);

        HashMap<String, String> qs = new HashMap<String, String>();
        qs.put("1", "What is 1+1?");
        qs.put("2", "Define a function.");

        HashMap<String, String> ans = new HashMap<String, String>();
        ans.put("1", "2");
        ans.put("2", "A mapping from inputs to outputs.");

        ExamPaper exam = new ExamPaper("EXAM_test", "Math", qs, "ANS_test");
        AnswerPaper answer = new AnswerPaper("ANS_test", "EXAM_test", "Math", qs, ans);

        dao.storeExamAnswer(exam, answer);

        ExamPaper loadedExam = dao.loadExamPaperById("EXAM_test");
        AnswerPaper loadedAns = dao.loadAnswerPaperById("ANS_test");

        assertNotNull(loadedExam);
        assertNotNull(loadedAns);
        assertEquals(exam, loadedExam);
        assertEquals(answer, loadedAns);
    }
}

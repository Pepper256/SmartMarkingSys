package dao;

import entities.MarkedStudentPaper;
import entities.Report;
import entities.StudentPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportDaoSqliteTest {

    private StudentPaperDao studentDao;
    private MarkedStudentPaperDao markedDao;
    private ReportDao reportDao;

    @BeforeEach
    void reset() {
        DaoTestUtil.resetSchemaAndClearAllTables();
        studentDao = new StudentPaperDao();
        markedDao = new MarkedStudentPaperDao();
        reportDao = new ReportDao();
    }

    private void seedOneMarkedPaper(String studentPaperId, String examPaperId) {
        HashMap<String, String> qs = new HashMap<String, String>();
        qs.put("1", "1+1=?");

        HashMap<String, String> rsp = new HashMap<String, String>();
        rsp.put("1", "2");

        StudentPaper sp = new StudentPaper(studentPaperId, examPaperId, "Math", qs, rsp, "coord", new HashMap<>());// TODO
        List<StudentPaper> sps = new ArrayList<StudentPaper>();
        sps.add(sp);
        studentDao.saveStudentPapers(sps);

        HashMap<String, Boolean> correctness = new HashMap<String, Boolean>();
        correctness.put("1", true);

        HashMap<String, String> reasons = new HashMap<String, String>();
        reasons.put("1", "");

        MarkedStudentPaper mp = new MarkedStudentPaper(
                studentPaperId,
                examPaperId,
                "Math",
                qs,
                rsp,
                "coord",
                new HashMap<>(),// TODO
                correctness,
                "nice work",
                reasons
        );
        List<MarkedStudentPaper> mps = new ArrayList<MarkedStudentPaper>();
        mps.add(mp);
        markedDao.saveAll(mps);
    }

    @Test
    void storeThenLoadReport_roundTrip() {
        Report r = new Report("R1", "EXAM_R", "jkljkl","report content");
        reportDao.storeReport(r);

        Report loaded = reportDao.getReportByReportId("R1");
        assertNotNull(loaded);
        assertEquals("R1", loaded.getId());
        assertEquals("EXAM_R", loaded.getExamPaperId());
        assertEquals("report content", loaded.getContent());
    }

    @Test
    void getMarkedStudentPaperById_returnsJoinedEntity() {
        seedOneMarkedPaper("SP_R1", "EXAM_R1");

        MarkedStudentPaper loaded = reportDao.getMarkedStudentPaperById("SP_R1");
        assertNotNull(loaded);
        assertEquals("SP_R1", loaded.getId());
        assertEquals("EXAM_R1", loaded.getExamPaperId());
        assertEquals("Math", loaded.getSubject());
        assertEquals("nice work", loaded.getMarkedContent());
        assertEquals(true, loaded.getCorrectness().get("1"));
    }

    @Test
    void getMarkedStudentPapersByExamPaperId_returnsList() {
        seedOneMarkedPaper("SP_L1", "EXAM_LIST");
        seedOneMarkedPaper("SP_L2", "EXAM_LIST");

        List<MarkedStudentPaper> list = reportDao.getMarkedStudentPapersByExamPaperId("EXAM_LIST");
        assertEquals(2, list.size());

        // Quick sanity check on one row
        assertTrue(list.stream().anyMatch(p -> "SP_L2".equals(p.getId())));
    }
}

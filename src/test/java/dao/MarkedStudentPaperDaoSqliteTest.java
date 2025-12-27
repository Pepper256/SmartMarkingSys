package dao;

import entities.MarkedStudentPaper;
import entities.StudentPaper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MarkedStudentPaperDaoSqliteTest {

    private StudentPaperDao studentDao;
    private MarkedStudentPaperDao markedDao;

    @BeforeEach
    void reset() {
        DaoTestUtil.resetSchemaAndClearAllTables();
        studentDao = new StudentPaperDao();
        markedDao = new MarkedStudentPaperDao();
    }

    @Test
    void saveThenFindByStudentPaperId_roundTrip() {
        // First insert student_paper (FK requirement)
        HashMap<String, String> qs = new HashMap<String, String>();
        qs.put("1", "1+1=?");

        HashMap<String, String> rsp = new HashMap<String, String>();
        rsp.put("1", "3");

        StudentPaper sp = new StudentPaper("SP_100", "EXAM_100", "Math", qs, rsp, "coord");
        List<StudentPaper> sps = new ArrayList<StudentPaper>();
        sps.add(sp);
        studentDao.saveStudentPapers(sps);

        // Then insert marked_student_paper.
        HashMap<String, Boolean> correctness = new HashMap<String, Boolean>();
        correctness.put("1", false);

        HashMap<String, String> reasons = new HashMap<String, String>();
        reasons.put("1", "calculation mistake");

        // NOTE: DAO convention: MarkedStudentPaper.id == student_paper_id
        MarkedStudentPaper mp = new MarkedStudentPaper(
                "SP_100",
                "EXAM_100",
                "Math",
                qs,
                rsp,
                "coord",
                correctness,
                "marked content",
                reasons
        );

        List<MarkedStudentPaper> mps = new ArrayList<MarkedStudentPaper>();
        mps.add(mp);
        markedDao.saveAll(mps);

        MarkedStudentPaperDao.MarkedStudentPaperRecord rec = markedDao.findByStudentPaperId("SP_100");
        assertNotNull(rec);

        assertEquals("SP_100", rec.studentPaperId);
        assertEquals("marked content", rec.markedContent);

        assertEquals(correctness, MapJsonUtil.toBooleanMap(rec.correctnessJson));
        assertEquals(reasons, MapJsonUtil.toStringMap(rec.reasonsJson));
    }
}

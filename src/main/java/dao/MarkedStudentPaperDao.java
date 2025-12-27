package dao;

import entities.MarkedStudentPaper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 批改结果 DAO（MarkedStudentPaper）。
 *
 * 设计：以 student_paper_id 作为唯一键，重复批改则覆盖更新。
 */
public class MarkedStudentPaperDao {

    public void saveAll(List<MarkedStudentPaper> markedPapers) {
        DatabaseManager.initSchemaIfNeeded();

        String sql = "INSERT OR REPLACE INTO marked_student_paper " +
                "(id, student_paper_id, marked_content, correctness_json, reasons_json) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MarkedStudentPaper mp : markedPapers) {
                    // 这里约定：mp.getId() 与 studentPaperId 一致
                    ps.setString(1, mp.getId());
                    ps.setString(2, mp.getId());
                    ps.setString(3, mp.getMarkedContent());
                    ps.setString(4, MapJsonUtil.toJson(mp.getCorrectness()));
                    ps.setString(5, MapJsonUtil.toJson(mp.getReasons()));
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("保存批改结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过 student_paper_id 获取批改结果。
     * 返回对象只包含 MarkedStudentPaper 新增字段（markedContent/correctness/reasons）；
     * 如需完整的 StudentPaper 字段请通过 StudentPaperDao 再取一次并组合。
     */
    public MarkedStudentPaperRecord findByStudentPaperId(String studentPaperId) {
        DatabaseManager.initSchemaIfNeeded();
        String sql = "SELECT id, student_paper_id, marked_content, correctness_json, reasons_json " +
                "FROM marked_student_paper WHERE student_paper_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentPaperId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new MarkedStudentPaperRecord(
                        rs.getString("id"),
                        rs.getString("student_paper_id"),
                        rs.getString("marked_content"),
                        rs.getString("correctness_json"),
                        rs.getString("reasons_json")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询批改结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为了不强行构造一个缺字段的 MarkedStudentPaper，这里先提供一个轻量记录对象。
     * 用例层如果需要完整 MarkedStudentPaper：
     * 1) 先用 StudentPaperDao.getStudentPaperById(studentPaperId)
     * 2) 再把这里的 correctness/reasons/markedContent 组合进去。
     */
    public static final class MarkedStudentPaperRecord {
        public final String id;
        public final String studentPaperId;
        public final String markedContent;
        public final String correctnessJson;
        public final String reasonsJson;

        public MarkedStudentPaperRecord(String id, String studentPaperId, String markedContent, String correctnessJson, String reasonsJson) {
            this.id = id;
            this.studentPaperId = studentPaperId;
            this.markedContent = markedContent;
            this.correctnessJson = correctnessJson;
            this.reasonsJson = reasonsJson;
        }
    }
}

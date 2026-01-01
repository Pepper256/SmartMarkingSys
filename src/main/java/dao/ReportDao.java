package dao;

import entities.MarkedStudentPaper;
import entities.Report;
import use_case.export_report.ExportReportDataAccessInterface;
import use_case.generate_report.GenerateReportDataAccessInterface;
import use_case.generate_student_report.GenerateStudentReportDataAccessInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Report persistence (SQLite).
 *
 * Responsibilities:
 * - Store and load generated reports
 * - Read marked student papers for report generation use cases
 */
public class ReportDao implements GenerateReportDataAccessInterface,
        GenerateStudentReportDataAccessInterface,
        ExportReportDataAccessInterface {

   @Override
public void storeReport(Report report) {
    Objects.requireNonNull(report, "report");
    DatabaseManager.initSchemaIfNeeded();

    String sql = "INSERT OR REPLACE INTO report " +
            "(id, exam_paper_id, student_paper_id, content, raw_json, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    long now = System.currentTimeMillis();

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, report.getId());
        ps.setString(2, report.getExamPaperId());
        ps.setString(3, report.getStudentPaperId());
        ps.setString(4, report.getContent());
        ps.setString(5, report.toJsonString());
        ps.setLong(6, now);

        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("保存报告失败: " + e.getMessage(), e);
    }
}


    @Override
    public Report getReportByReportId(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) return null;
        DatabaseManager.initSchemaIfNeeded();

        String sql = "SELECT id, exam_paper_id, student_paper_id, content FROM report WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Report(
                        rs.getString("id"),
                        rs.getString("exam_paper_id"),
                        rs.getString("student_paper_id"),
                        rs.getString("content")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询报告失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MarkedStudentPaper getMarkedStudentPaperById(String markedStudentPaperId) {
        if (markedStudentPaperId == null || markedStudentPaperId.trim().isEmpty()) return null;
        DatabaseManager.initSchemaIfNeeded();

        String sql = "SELECT " +
                "sp.id AS sp_id, sp.exam_paper_id, sp.subject, sp.questions_json, sp.responses_json, sp.coord_content, sp.paper_base64_json, " +
                "mp.marked_content, mp.correctness_json, mp.reasons_json " +
                "FROM student_paper sp " +
                "JOIN marked_student_paper mp ON mp.student_paper_id = sp.id " +
                "WHERE mp.id = ? OR mp.student_paper_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, markedStudentPaperId);
            ps.setString(2, markedStudentPaperId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return buildMarkedStudentPaperFromJoin(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询已批改试卷失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MarkedStudentPaper> getMarkedStudentPapersByExamPaperId(String examPaperId) {
        if (examPaperId == null || examPaperId.trim().isEmpty()) {
            return new ArrayList<MarkedStudentPaper>();
        }
        DatabaseManager.initSchemaIfNeeded();

        String sql = "SELECT " +
                "sp.id AS sp_id, sp.exam_paper_id, sp.subject, sp.questions_json, sp.responses_json, sp.coord_content, sp.paper_base64_json, " +
                "mp.marked_content, mp.correctness_json, mp.reasons_json " +
                "FROM student_paper sp " +
                "JOIN marked_student_paper mp ON mp.student_paper_id = sp.id " +
                "WHERE sp.exam_paper_id = ?";

        List<MarkedStudentPaper> out = new ArrayList<MarkedStudentPaper>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, examPaperId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(buildMarkedStudentPaperFromJoin(rs));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("按 examPaperId 查询已批改试卷失败: " + e.getMessage(), e);
        }
    }

    private MarkedStudentPaper buildMarkedStudentPaperFromJoin(ResultSet rs) throws SQLException {
        String id = rs.getString("sp_id");
        String examPaperId = rs.getString("exam_paper_id");
        String subject = rs.getString("subject");

        String questionsJson = rs.getString("questions_json");
        String responsesJson = rs.getString("responses_json");
        String coordContent = rs.getString("coord_content");
        String paperBase64Json = rs.getString("paper_base64_json");

        String markedContent = rs.getString("marked_content");
        String correctnessJson = rs.getString("correctness_json");
        String reasonsJson = rs.getString("reasons_json");

        return new MarkedStudentPaper(
                id,
                examPaperId,
                subject,
                MapJsonUtil.toStringMap(questionsJson),
                MapJsonUtil.toStringMap(responsesJson),
                coordContent,
                MapJsonUtil.toStringMap(paperBase64Json),
                MapJsonUtil.toBooleanMap(correctnessJson),
                markedContent,
                MapJsonUtil.toStringMap(reasonsJson)
        );
    }
}

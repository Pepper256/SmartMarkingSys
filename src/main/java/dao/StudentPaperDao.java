package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import entities.AnswerPaper;
import entities.ExamPaper;
import entities.MarkedStudentPaper;
import entities.StudentPaper;
import use_case.auto_marking.AutoMarkingDataAccessInterface;
import use_case.upload_student_answer.UploadStudentAnswerDataAccessInterface;

/**
 * 学生答卷 DAO： - UploadStudentAnswerUseCase 通过它保存学生答卷 - AutoMarkingUseCase
 * 通过它读取学生答卷并保存批改结果
 */
public class StudentPaperDao implements UploadStudentAnswerDataAccessInterface, AutoMarkingDataAccessInterface {

    private final MarkedStudentPaperDao markedDao = new MarkedStudentPaperDao();

    @Override
    public void saveStudentPapers(List<StudentPaper> studentPapers) {
        DatabaseManager.initSchemaIfNeeded();
        String sql = "INSERT OR REPLACE INTO student_paper (id, exam_paper_id, subject, questions_json, responses_json, coord_content) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (StudentPaper sp : studentPapers) {
                    ps.setString(1, sp.getId());
                    ps.setString(2, sp.getExamPaperId());
                    ps.setString(3, sp.getSubject());
                    ps.setString(4, MapJsonUtil.toJson(sp.getQuestions()));
                    ps.setString(5, MapJsonUtil.toJson(sp.getResponses()));
                    ps.setString(6, sp.getCoordContent());
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
            throw new RuntimeException("保存学生答卷失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ExamPaper getExamPaperById(String examPaperId) {
        DatabaseManager.initSchemaIfNeeded();

        String sql = "SELECT raw_json FROM exam_paper WHERE id = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, examPaperId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String rawJson = rs.getString("raw_json");
                ExamPaper exam = ExamPaper.jsonToExamPaper(rawJson);

                // JSON 解析失败属于严重数据问题，直接报错更好定位
                if (exam == null) {
                    throw new RuntimeException("ExamPaper JSON 解析失败, id=" + examPaperId);
                }
                return exam;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询试卷失败: " + e.getMessage(), e);
        }
    }

    @Override
    public StudentPaper getStudentPaperById(String id) {
        DatabaseManager.initSchemaIfNeeded();
        String sql = "SELECT id, exam_paper_id, subject, questions_json, responses_json, coord_content FROM student_paper WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new StudentPaper(
                        rs.getString("id"),
                        rs.getString("exam_paper_id"),
                        rs.getString("subject"),
                        MapJsonUtil.toStringMap(rs.getString("questions_json")),
                        MapJsonUtil.toStringMap(rs.getString("responses_json")),
                        rs.getString("coord_content")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询学生答卷失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void storeMarkedPapers(List<MarkedStudentPaper> studentPapers) {
        // AutoMarkingUseCase 批改完成后会调用这个方法
        markedDao.saveAll(studentPapers);
    }

    @Override
    public AnswerPaper getAnswerPaperByExamPaperId(String examPaperId) {
        DatabaseManager.initSchemaIfNeeded();
        String sql = "SELECT raw_json FROM answer_paper WHERE exam_paper_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, examPaperId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String rawJson = rs.getString("raw_json");
                return AnswerPaper.jsonToAnswerPaper(rawJson);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询答案卷失败: " + e.getMessage(), e);
        }
    }
}

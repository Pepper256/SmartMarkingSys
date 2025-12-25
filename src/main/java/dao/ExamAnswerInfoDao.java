package dao;

import entities.AnswerPaper;
import entities.ExamPaper;
import use_case.upload_paper_answer.UploadPaperAnswerDataAccessInterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 负责存取“空白试卷(ExamPaper)”与“标准答案(AnswerPaper)”。
 *
 * 说明：当前 UploadPaperAnswerUseCase 只要求 storeExamAnswer(...)。
 * 为了后续用例（如自动批改/报告）方便，这里也提供了常用查询方法。
 */
public class ExamAnswerInfoDao implements UploadPaperAnswerDataAccessInterface {

    @Override
    public void storeExamAnswer(ExamPaper examPaper, AnswerPaper answerPaper) {
        DatabaseManager.initSchemaIfNeeded();

        String insertExam = "INSERT OR REPLACE INTO exam_paper (id, subject, answer_id, questions_json) VALUES (?, ?, ?, ?)";
        String insertAnswer = "INSERT OR REPLACE INTO answer_paper (id, exam_paper_id, subject, questions_json, answers_json) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psExam = conn.prepareStatement(insertExam);
                 PreparedStatement psAns = conn.prepareStatement(insertAnswer)) {

                psExam.setString(1, examPaper.getId());
                psExam.setString(2, examPaper.getSubject());
                psExam.setString(3, examPaper.getAnswerId());
                psExam.setString(4, MapJsonUtil.toJson(examPaper.getQuestions()));
                psExam.executeUpdate();

                psAns.setString(1, answerPaper.getId());
                psAns.setString(2, answerPaper.getExamPaperId());
                psAns.setString(3, answerPaper.getSubject());
                psAns.setString(4, MapJsonUtil.toJson(answerPaper.getQuestions()));
                psAns.setString(5, MapJsonUtil.toJson(answerPaper.getAnswers()));
                psAns.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("写入试卷/答案失败: " + e.getMessage(), e);
        }
    }

    public ExamPaper findExamPaperById(String id) {
        String sql = "SELECT id, subject, answer_id, questions_json FROM exam_paper WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ExamPaper(
                        rs.getString("id"),
                        rs.getString("subject"),
                        MapJsonUtil.toStringMap(rs.getString("questions_json")),
                        rs.getString("answer_id")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询试卷失败: " + e.getMessage(), e);
        }
    }

    public AnswerPaper findAnswerPaperById(String id) {
        String sql = "SELECT id, exam_paper_id, subject, questions_json, answers_json FROM answer_paper WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new AnswerPaper(
                        rs.getString("id"),
                        rs.getString("exam_paper_id"),
                        rs.getString("subject"),
                        MapJsonUtil.toStringMap(rs.getString("questions_json")),
                        MapJsonUtil.toStringMap(rs.getString("answers_json"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询答案失败: " + e.getMessage(), e);
        }
    }

    public AnswerPaper findAnswerByExamPaperId(String examPaperId) {
        String sql = "SELECT id, exam_paper_id, subject, questions_json, answers_json FROM answer_paper WHERE exam_paper_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, examPaperId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new AnswerPaper(
                        rs.getString("id"),
                        rs.getString("exam_paper_id"),
                        rs.getString("subject"),
                        MapJsonUtil.toStringMap(rs.getString("questions_json")),
                        MapJsonUtil.toStringMap(rs.getString("answers_json"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询答案失败: " + e.getMessage(), e);
        }
    }
}

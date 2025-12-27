package dao;

import entities.AnswerPaper;
import entities.ExamPaper;
import use_case.Constants;
import use_case.upload_paper_answer.UploadPaperAnswerDataAccessInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQLite-backed DAO.
 *
 * What this guarantees (the "this must be a usable database" part):
 * - Creates a real SQLite .db file (default: ./database/smartmark.db)
 * - Creates tables automatically on first use (schema.sql)
 * - Uses transactions for atomic writes
 */
public class ExamAnswerInfoDao implements UploadPaperAnswerDataAccessInterface {

    private static final String SCHEMA_RESOURCE = "database/schema.sql";

    private final Path dbFilePath;

    public ExamAnswerInfoDao() {
        this(Constants.SQLITE_DB_PATH);
    }

    public ExamAnswerInfoDao(String dbFilePath) {
        this.dbFilePath = Paths.get(Objects.requireNonNull(dbFilePath, "dbFilePath"));
    }

    /**
     * Store one exam paper and its corresponding official answer paper.
     * The write is transactional: either both rows are written or none.
     */
    @Override
    public void storeExamAnswer(ExamPaper examPaper, AnswerPaper answerPaper) {
        Objects.requireNonNull(examPaper, "examPaper");
        Objects.requireNonNull(answerPaper, "answerPaper");

        if (isBlank(examPaper.getId())) {
            throw new IllegalArgumentException("examPaper.id is blank");
        }
        if (isBlank(answerPaper.getId())) {
            throw new IllegalArgumentException("answerPaper.id is blank");
        }

        // If upstream forgot to set the link, fix it defensively.
        if (isBlank(examPaper.getAnswerId())) {
            examPaper.setAnswerId(answerPaper.getId());
        }
        if (isBlank(answerPaper.getExamPaperId())) {
            answerPaper.setExamPaperId(examPaper.getId());
        }

        long now = System.currentTimeMillis();

        try (Connection conn = openConnection()) {
            initSchemaIfNeeded(conn);

            conn.setAutoCommit(false);
            try {
                upsertExamPaper(conn, examPaper, now);
                upsertAnswerPaper(conn, answerPaper, now);
                conn.commit();
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // ignore rollback failure; original exception is more important
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // ignore
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store exam/answer into SQLite database: " + dbFilePath, e);
        }
    }

    /**
     * Convenience method for verification / debugging.
     */
    public Path getDatabaseFilePath() {
        return dbFilePath.toAbsolutePath().normalize();
    }

    /**
     * Load an exam paper by id (null if not found).
     */
    public ExamPaper loadExamPaperById(String examPaperId) {
        if (isBlank(examPaperId)) return null;

        String sql = "SELECT raw_json FROM exam_paper WHERE id = ?";
        try (Connection conn = openConnection()) {
            initSchemaIfNeeded(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, examPaperId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    String rawJson = rs.getString(1);
                    return ExamPaper.jsonToExamPaper(rawJson);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load exam_paper: " + examPaperId, e);
        }
    }

    /**
     * Load an answer paper by id (null if not found).
     */
    public AnswerPaper loadAnswerPaperById(String answerPaperId) {
        if (isBlank(answerPaperId)) return null;

        String sql = "SELECT raw_json FROM answer_paper WHERE id = ?";
        try (Connection conn = openConnection()) {
            initSchemaIfNeeded(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, answerPaperId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    String rawJson = rs.getString(1);
                    return AnswerPaper.jsonToAnswerPaper(rawJson);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load answer_paper: " + answerPaperId, e);
        }
    }

    /**
     * Danger: for tests only.
     */
    public void deleteAll() {
        try (Connection conn = openConnection()) {
            initSchemaIfNeeded(conn);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM answer_paper");
                st.executeUpdate("DELETE FROM exam_paper");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }

    // -------------------- Internal helpers --------------------

    private Connection openConnection() throws SQLException, IOException {
        Path abs = getDatabaseFilePath();

        // Ensure parent directory exists (./database by default)
        Path parent = abs.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        String url = "jdbc:sqlite:" + abs.toString();
        Connection conn = DriverManager.getConnection(url);

        // Connection-local safety settings
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            // WAL improves reliability for concurrent read + occasional writes.
            st.execute("PRAGMA journal_mode = WAL");
        }

        return conn;
    }

    private void initSchemaIfNeeded(Connection conn) throws SQLException {
        List<String> statements = readSchemaStatementsFromResource();

        try (Statement st = conn.createStatement()) {
            for (String s : statements) {
                st.execute(s);
            }
        }
    }

    private List<String> readSchemaStatementsFromResource() {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE);
        if (in == null) {
            // Fallback: minimal schema embedded (keeps DAO usable even if resource packaging is wrong)
            List<String> fallback = new ArrayList<String>();
            fallback.add("CREATE TABLE IF NOT EXISTS exam_paper (id TEXT PRIMARY KEY, subject TEXT, questions_json TEXT NOT NULL, answer_id TEXT, raw_json TEXT NOT NULL, created_at INTEGER NOT NULL)");
            fallback.add("CREATE TABLE IF NOT EXISTS answer_paper (id TEXT PRIMARY KEY, exam_paper_id TEXT NOT NULL, subject TEXT, questions_json TEXT NOT NULL, answers_json TEXT NOT NULL, raw_json TEXT NOT NULL, created_at INTEGER NOT NULL, FOREIGN KEY (exam_paper_id) REFERENCES exam_paper(id))");
            fallback.add("CREATE INDEX IF NOT EXISTS idx_answer_paper_exam_id ON answer_paper(exam_paper_id)");
            return fallback;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Remove line comments
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema resource: " + SCHEMA_RESOURCE, e);
        }

        return splitSqlStatements(sb.toString());
    }

    private List<String> splitSqlStatements(String sqlText) {
        List<String> out = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < sqlText.length(); i++) {
            char c = sqlText.charAt(i);
            if (c == ';') {
                String s = cur.toString().trim();
                if (!s.isEmpty()) out.add(s);
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        String last = cur.toString().trim();
        if (!last.isEmpty()) out.add(last);
        return out;
    }

    private void upsertExamPaper(Connection conn, ExamPaper examPaper, long createdAt) throws SQLException {
        String sql = "INSERT OR REPLACE INTO exam_paper (id, subject, questions_json, answer_id, raw_json, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, examPaper.getId());
            ps.setString(2, nullToEmpty(examPaper.getSubject()));
            ps.setString(3, safeJson(examPaper.getQuestions()));
            ps.setString(4, nullToEmpty(examPaper.getAnswerId()));
            ps.setString(5, examPaper.toJsonString());
            ps.setLong(6, createdAt);
            ps.executeUpdate();
        }
    }

    private void upsertAnswerPaper(Connection conn, AnswerPaper answerPaper, long createdAt) throws SQLException {
        String sql = "INSERT OR REPLACE INTO answer_paper (id, exam_paper_id, subject, questions_json, answers_json, raw_json, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, answerPaper.getId());
            ps.setString(2, nullToEmpty(answerPaper.getExamPaperId()));
            ps.setString(3, nullToEmpty(answerPaper.getSubject()));
            ps.setString(4, safeJson(answerPaper.getQuestions()));
            ps.setString(5, safeJson(answerPaper.getAnswers()));
            ps.setString(6, answerPaper.toJsonString());
            ps.setLong(7, createdAt);
            ps.executeUpdate();
        }
    }

    private String safeJson(Object maybeMap) {
        // We store these maps as JSON inside TEXT columns to keep the schema stable.
        // fastjson already exists in the project and is used by entity .toJsonString().
        try {
            // Avoid importing fastjson here to keep this DAO dependency-light.
            // Fall back to String.valueOf if needed.
            return com.alibaba.fastjson.JSON.toJSONString(maybeMap);
        } catch (Throwable t) {
            return String.valueOf(maybeMap);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}

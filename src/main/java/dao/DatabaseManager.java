package dao;

import use_case.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** SQLite 数据库连接与建表初始化。 */
public final class DatabaseManager {
    private static volatile boolean initialized = false;

    private DatabaseManager() {}

    /** 获取数据库连接（会自动建表一次）。 */
    public static Connection getConnection() throws SQLException {
        initSchemaIfNeeded();
        return DriverManager.getConnection(Constants.DB_URL);
    }

    /** 初始化表结构（幂等，重复调用安全）。 */
    public static void initSchemaIfNeeded() {
        if (initialized) return;
        synchronized (DatabaseManager.class) {
            if (initialized) return;
            try (Connection conn = DriverManager.getConnection(Constants.DB_URL);
                 Statement st = conn.createStatement()) {

                st.executeUpdate("CREATE TABLE IF NOT EXISTS exam_paper (" +
                        "id TEXT PRIMARY KEY," +
                        "subject TEXT," +
                        "answer_id TEXT," +
                        "questions_json TEXT" +
                        ")");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS answer_paper (" +
                        "id TEXT PRIMARY KEY," +
                        "exam_paper_id TEXT," +
                        "subject TEXT," +
                        "questions_json TEXT," +
                        "answers_json TEXT" +
                        ")");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS student_paper (" +
                        "id TEXT PRIMARY KEY," +
                        "exam_paper_id TEXT," +
                        "subject TEXT," +
                        "questions_json TEXT," +
                        "responses_json TEXT," +
                        "coord_content TEXT" +
                        ")");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS marked_student_paper (" +
                        "id TEXT PRIMARY KEY," +
                        "student_paper_id TEXT UNIQUE," +
                        "marked_content TEXT," +
                        "correctness_json TEXT," +
                        "reasons_json TEXT" +
                        ")");

                initialized = true;
            } catch (SQLException e) {
                throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
            }
        }
    }
}

package dao;

import use_case.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test helper for DAO tests.
 *
 * These tests intentionally use the project's default SQLite DB path
 * (Constants.SQLITE_DB_PATH, e.g. ./database/smartmark.db) so you can open it
 * with HeidiSQL after running tests.
 */
public final class DaoTestUtil {

    private DaoTestUtil() {}

    public static Path dbPath() {
        return Paths.get(Constants.SQLITE_DB_PATH).toAbsolutePath().normalize();
    }

    /**
     * Ensure schema exists, and clear all tables so each test class starts from a known state.
     */
    public static void resetSchemaAndClearAllTables() {
        // Ensure schema exists
        DatabaseManager.initSchemaIfNeeded();

        // Clear in dependency order (children first)
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM marked_student_paper");
            st.executeUpdate("DELETE FROM student_paper");
            st.executeUpdate("DELETE FROM report");
            st.executeUpdate("DELETE FROM answer_paper");
            st.executeUpdate("DELETE FROM exam_paper");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear tables for DAO tests", e);
        }
    }

    /**
     * Create parent folder of the DB if needed.
     */
    public static void ensureDbFolderExists() {
        try {
            Path p = dbPath();
            Path parent = p.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure database folder exists", e);
        }
    }
}

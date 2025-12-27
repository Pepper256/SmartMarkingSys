package dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerSqliteTest {

    @BeforeAll
    static void setup() {
        DaoTestUtil.ensureDbFolderExists();
        // start from clean tables so other DAO tests can rely on predictable state
        DaoTestUtil.resetSchemaAndClearAllTables();
    }

    @Test
    void createsDatabaseFileAndSchemaTables() throws Exception {
        // Opening a connection should create the .db file and initialize schema.
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement()) {
            // Verify the main tables exist
            assertTrue(tableExists(st, "exam_paper"));
            assertTrue(tableExists(st, "answer_paper"));
            assertTrue(tableExists(st, "student_paper"));
            assertTrue(tableExists(st, "marked_student_paper"));
            assertTrue(tableExists(st, "report"));
        }

        assertTrue(Files.exists(DaoTestUtil.dbPath()), "Expected SQLite file to exist at " + DaoTestUtil.dbPath());
    }

    private boolean tableExists(Statement st, String name) throws Exception {
        try (ResultSet rs = st.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + name + "'")) {
            return rs.next();
        }
    }
}

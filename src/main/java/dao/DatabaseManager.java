package dao;

import use_case.Constants;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared SQLite connection + schema initializer.
 *
 * Important: the schema is sourced from {@code src/main/resources/database/schema.sql}.
 * This keeps all DAOs aligned on the same tables/columns.
 */
public final class DatabaseManager {

    private static final String SCHEMA_RESOURCE = "database/schema.sql";

    private static volatile boolean initialized = false;

    private DatabaseManager() {}

    /**
     * Get a connection to the configured SQLite database.
     * The schema will be initialized once (idempotent).
     */
    public static Connection getConnection() throws SQLException {
        initSchemaIfNeeded();
        return openConnectionNoInit();
    }

    /** Initialize schema once. Safe to call many times. */
    public static void initSchemaIfNeeded() {
        if (initialized) return;
        synchronized (DatabaseManager.class) {
            if (initialized) return;
            try (Connection conn = openConnectionNoInit();
                 Statement st = conn.createStatement()) {

                for (String stmt : readSchemaStatementsFromResource()) {
                    st.execute(stmt);
                }

                // Lightweight migrations for newly added columns.
                // Note: CREATE TABLE IF NOT EXISTS will NOT add columns to existing tables.
                ensureColumnExists(conn, "student_paper", "paper_base64_json", "TEXT");
                ensureColumnExists(conn, "report", "student_paper_id", "TEXT");

                initialized = true;
            } catch (SQLException | IOException e) {
                throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
            }
        }
    }

    // -------------------- internals --------------------

    private static Connection openConnectionNoInit() throws SQLException {
        Path abs = Paths.get(Constants.SQLITE_DB_PATH).toAbsolutePath().normalize();

        // Ensure parent directory exists (e.g. ./database)
        Path parent = abs.getParent();
        try {
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建数据库目录: " + parent, e);
        }

        String url = "jdbc:sqlite:" + abs;
        Connection conn = DriverManager.getConnection(url);

        // Connection-local safety settings
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
        }

        return conn;
    }

    private static List<String> readSchemaStatementsFromResource() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_RESOURCE);
        if (in == null) {
            throw new IOException("找不到 schema 资源文件: " + SCHEMA_RESOURCE);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                sb.append(line).append('\n');
            }
        }

        return splitSqlStatements(sb.toString());
    }

    private static List<String> splitSqlStatements(String sqlText) {
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


    // ---- schema migrations ----
    // These are intentionally lightweight. We only add columns that are required by current DAO code.
    private static void ensureColumnExists(Connection conn, String table, String column, String columnType) throws SQLException {
        if (columnExists(conn, table, column)) return;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnType);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null && name.equalsIgnoreCase(column)) {
                    return true;
                }
            }
            return false;
        }
    }

}
package app;

import dao.ExamAnswerInfoDao;
import entities.AnswerPaper;
import entities.ExamPaper;

import java.util.HashMap;

import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        // Quick demo to verify SQLite works without reading code:
        // Run with: Main demo-db
        if (args != null && args.length > 0 && "demo-db".equals(args[0])) {
            ExamAnswerInfoDao dao = new ExamAnswerInfoDao();

            HashMap<String, String> qs = new HashMap<String, String>();
            qs.put("1", "1+1=?");

            HashMap<String, String> as = new HashMap<String, String>();
            as.put("1", "2");

            ExamPaper exam = new ExamPaper("EXAM_demo", "Math", qs, "ANS_demo");
            AnswerPaper ans = new AnswerPaper("ANS_demo", "EXAM_demo", "Math", qs, as);

            dao.storeExamAnswer(exam, ans);
            System.out.println("SQLite DB written at: " + dao.getDatabaseFilePath());
            return;
        }

        System.out.println("Hello world");
    }

    public static String loadQwenApiKey() {
        Properties config = new Properties();
        try (InputStream input = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources folder");
            }
            config.load(input);
            String key = config.getProperty("qwen.api.key");
            if (key == null || key.trim().isEmpty()) {
                throw new RuntimeException("qwen.api.key not found in config.properties");
            }
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load API key: " + e.getMessage(), e);
        }
    }
}

package app;

import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
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

    public static String loadDeepseekApiKey() {
        Properties config = new Properties();
        try (InputStream input = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources folder");
            }
            config.load(input);
            String key = config.getProperty("deepseek.api.key");
            if (key == null || key.trim().isEmpty()) {
                throw new RuntimeException("deepseek.api.key not found in config.properties");
            }
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load API key: " + e.getMessage(), e);
        }
    }
}

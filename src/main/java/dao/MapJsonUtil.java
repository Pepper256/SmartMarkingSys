package dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

/**
 * 将 Map 等复杂字段转换成 JSON 字符串存数据库；再从 JSON 字符串还原。
 */
public final class MapJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MapJsonUtil() {}

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    public static HashMap<String, String> toStringMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<HashMap<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化为 Map<String,String> 失败: " + e.getMessage(), e);
        }
    }

    public static HashMap<String, Boolean> toBooleanMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<HashMap<String, Boolean>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化为 Map<String,Boolean> 失败: " + e.getMessage(), e);
        }
    }
}

package use_case.util;

public class FileUtil {
    
    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // 1. 获取最后一个点的位置
        int lastDotIndex = filePath.lastIndexOf('.');

        // 2. 获取最后一个路径分隔符的位置（兼容 Windows 和 Unix）
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        // 3. 逻辑判断：
        // 点必须存在，且点必须在最后一个路径分隔符之后
        // 并且点不能是字符串的最后一个字符
        if (lastDotIndex > lastSeparatorIndex && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        }

        return ""; // 没有后缀名
    }
}

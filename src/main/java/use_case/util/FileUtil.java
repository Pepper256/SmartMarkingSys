package use_case.util;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import use_case.Constants;
import use_case.export_report.ExportReportUseCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

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

    public static void convertMarkdownToPdf(String markdownContent, String destPath) throws Exception {
        // 1. Flexmark: Markdown -> HTML
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        String rawHtml = renderer.render(parser.parse(markdownContent));

        // 2. 注入 CSS 样式，确保 HTML 使用我们注册的字体
        String processedHtml = "<html><head><style>" +
                "body { font-family: '" + Constants.FONT_FAMILY_NAME + "', sans-serif; }" +
                "</style></head><body>" +
                rawHtml +
                "</body></html>";

        // 3. OpenHTMLtoPDF: HTML -> PDF
        try (OutputStream os = new FileOutputStream(destPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // --- 解决中文乱码：从 Resources 加载字体 ---
            builder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    // 使用类加载器读取 resources 下的文件
                    InputStream is = ExportReportUseCase.class.getResourceAsStream(Constants.FONT_RESOURCE_PATH);
                    if (is == null) {
                        throw new RuntimeException("找不到字体文件: " + Constants.FONT_RESOURCE_PATH);
                    }
                    return is;
                }
            }, Constants.FONT_FAMILY_NAME);

            builder.withHtmlContent(processedHtml, null);
            builder.toStream(os);
            builder.run();
        }
    }

    public static String saveImageToConstPath(String base64String, String fileName) throws IOException {
        // 1. 处理 Base64 前缀
        if (base64String.contains(",")) {
            base64String = base64String.split(",")[1];
        }

        // 2. 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64String);

        // 3. 转换为 BufferedImage
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Base64 数据无法解析为有效的图片");
            }

            // 4. 调用版本 A 的函数进行实际保存，实现代码复用
            return saveImageToConstPath(image, fileName);
        }
    }

    public static BufferedImage base64ToBufferedImage(String base64String) throws IOException {
        if (base64String == null || base64String.isEmpty()) {
            throw new IllegalArgumentException("Base64 string must not be null or empty");
        }

        // 1. 兼容性处理：去除 Data URI 前缀 (例如 "data:image/png;base64,")
        String pureBase64 = base64String.contains(",")
                ? base64String.split(",")[1]
                : base64String;

        // 2. 解码 Base64 字符串
        byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

        // 3. 使用 ByteArrayInputStream 读取为 BufferedImage
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);

            if (image == null) {
                throw new IOException("Base64 data could not be decoded into a valid image.");
            }
            return image;
        }
    }

    public static String saveImageToConstPath(BufferedImage image, String fileName) throws IOException {
        File directory = new File(Constants.DOWNLOAD_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File outputFile = new File(directory, fileName);
        boolean success = ImageIO.write(image, "png", outputFile);

        if (!success) {
            throw new IOException("图片写入失败");
        }
        return outputFile.getAbsolutePath();
    }
}

package use_case.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class LayoutConvertUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 默认常量（根据 Python 代码同步）
    private static final int DEFAULT_FACTOR = 28;
    private static final int DEFAULT_MIN_PIXELS = 3136;
    private static final int DEFAULT_MAX_PIXELS = 11289600;

    public static List<Map<String, Object>> postProcessCells(
            BufferedImage originImage,
            List<Map<String, Object>> cells,
            int inputWidth,  // 发送给服务器时的图像宽度
            int inputHeight // 发送给服务器时的图像高度
    ) {

        if (cells == null || cells.isEmpty()) return cells;

        int originalWidth = originImage.getWidth();
        int originalHeight = originImage.getHeight();

        // 1. 计算服务器端 Smart Resize 后的实际目标尺寸 (h_bar, w_bar)
        int[] resizedDims = smartResize(inputHeight,
                inputWidth
        );
        int hBar = resizedDims[0];
        int wBar = resizedDims[1];

        // 2. 计算缩放比例 (模型输出坐标基于 wBar/hBar)
        double scaleX = (double) wBar / originalWidth;
        double scaleY = (double) hBar / originalHeight;

        // 3. 遍历并还原坐标
        for (Map<String, Object> cell : cells) {
            Object bboxObj = cell.get("bbox");
            if (bboxObj instanceof List) {
                List<Number> bbox = (List<Number>) bboxObj;
                List<Integer> bboxOriginal = new ArrayList<>();

                // 还原逻辑：原始坐标 = 模型坐标 / 缩放比例
                bboxOriginal.add((int) (bbox.get(0).doubleValue() / scaleX)); // x1
                bboxOriginal.add((int) (bbox.get(1).doubleValue() / scaleY)); // y1
                bboxOriginal.add((int) (bbox.get(2).doubleValue() / scaleX)); // x2
                bboxOriginal.add((int) (bbox.get(3).doubleValue() / scaleY)); // y2

                cell.put("bbox", bboxOriginal);
            }
        }
        return cells;
    }

    private static int[] smartResize(int height, int width) {


        int hBar = Math.max(DEFAULT_FACTOR, (int) (Math.round((double) height / DEFAULT_FACTOR) * DEFAULT_FACTOR));
        int wBar = Math.max(DEFAULT_FACTOR, (int) (Math.round((double) width / DEFAULT_FACTOR) * DEFAULT_FACTOR));

        if ((long) hBar * wBar > DEFAULT_MAX_PIXELS) {
            double beta = Math.sqrt(((double) height * width) / DEFAULT_MAX_PIXELS);
            hBar = Math.max(DEFAULT_FACTOR, (int) (Math.floor(height / beta / DEFAULT_FACTOR) * DEFAULT_FACTOR));
            wBar = Math.max(DEFAULT_FACTOR, (int) (Math.floor(width / beta / DEFAULT_FACTOR) * DEFAULT_FACTOR));
        } else if ((long) hBar * wBar < DEFAULT_MIN_PIXELS) {
            double beta = Math.sqrt((double) DEFAULT_MIN_PIXELS / ((double) height * width));
            hBar = (int) (Math.ceil(height * beta / DEFAULT_FACTOR) * DEFAULT_FACTOR);
            wBar = (int) (Math.ceil(width * beta / DEFAULT_FACTOR) * DEFAULT_FACTOR);

            if ((long) hBar * wBar > DEFAULT_MAX_PIXELS) {
                double beta2 = Math.sqrt(((double) hBar * wBar) / DEFAULT_MAX_PIXELS);
                hBar = Math.max(DEFAULT_FACTOR, (int) (Math.floor(hBar / beta2 / DEFAULT_FACTOR) * DEFAULT_FACTOR));
                wBar = Math.max(DEFAULT_FACTOR, (int) (Math.floor(wBar / beta2 / DEFAULT_FACTOR) * DEFAULT_FACTOR));
            }
        }
        return new int[]{hBar, wBar};
    }

    public static String layoutJson2Md(
            BufferedImage image,
            List<Map<String, Object>> cells,
            String textKey,
            boolean noPageHf
    ) throws IOException {
        List<String> textItems = new ArrayList<>();
        int imgW = image.getWidth();
        int imgH = image.getHeight();

        for (Map<String, Object> cell : cells) {
            String category = (String) cell.get("category");
            String text = cell.containsKey(textKey) ? (String) cell.get(textKey) : "";

            // 1. 过滤页眉页脚
            if (noPageHf && ("Page-header".equals(category) || "Page-footer".equals(category))) {
                continue;
            }

            // 2. 获取已还原的坐标
            List<Integer> bbox = (List<Integer>) cell.get("bbox");
            int x1 = bbox.get(0);
            int y1 = bbox.get(1);
            int x2 = bbox.get(2);
            int y2 = bbox.get(3);

            // 3. 根据类别处理
            if ("Picture".equals(category)) {
                // 修正 Java getSubimage 的边界安全 (x, y, w, h)
//                int safeX1 = Math.max(0, x1);
//                int safeY1 = Math.max(0, y1);
//                int width = Math.min(x2 - x1, imgW - safeX1);
//                int height = Math.min(y2 - y1, imgH - safeY1);
//
//                if (width > 0 && height > 0) {
//                    BufferedImage crop = image.getSubimage(safeX1, safeY1, width, height);
//                    String base64 = imageToBase64(crop);
//                    textItems.add("![](" + base64 + ")");
//                }
                continue;
            } else if ("Formula".equals(category)) {
                // 包装 LaTeX 公式
                textItems.add("$" + text + "$");
            } else {
                // 普通文本清洗后添加
                textItems.add(text.trim());
            }
        }

        // 4. 合并 Markdown 文本
        return String.join("\n\n", textItems);
    }

    private static String imageToBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static int[] getResizedDimensions(
            BufferedImage originImage
    ) {
        int width = originImage.getWidth();
        int height = originImage.getHeight();

        return smartResize(height, width);
    }

    public static List<Map<String, Object>> resultToCells(String ocrResult) {
        if (ocrResult == null || ocrResult.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 使用 TypeReference 明确指定解析目标为 List<Map<String, Object>>
            return OBJECT_MAPPER.readValue(ocrResult, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // 在实际业务中，建议记录日志
            System.err.println("解析 OCR 结果 JSON 失败: " + e.getMessage());
            // 如果解析失败，根据业务需求返回空列表或抛出运行时异常
            return new ArrayList<>();
        }
    }
}

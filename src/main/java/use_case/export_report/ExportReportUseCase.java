package use_case.export_report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import entities.Report;
import use_case.Constants;
import use_case.dto.ExportReportInputData;
import use_case.dto.ExportReportOutputData;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ExportReportUseCase implements ExportReportInputBoundary{

    private final ExportReportOutputBoundary outputBoundary;
    private final ExportReportDataAccessInterface dao;

    public ExportReportUseCase(ExportReportOutputBoundary outputBoundary,
                               ExportReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(ExportReportInputData inputData) {
        String reportId = inputData.getReportId();

        try {
            // 1. 获取报告数据
            Report report = dao.getReportByReportId(reportId);
            if (report == null || report.getContent() == null) {
                throw new IllegalArgumentException("未找到指定的报告内容");
            }

            // 2. 生成唯一文件名 (增加 .pdf 后缀)
            String fileName = "report_" + reportId + ".pdf";

            // 3. 执行导出
            exportMarkdownToPdf(report.getContent(), fileName);

            // 4. 只有执行成功才进入成功视图
            outputBoundary.prepareSuccessView(new ExportReportOutputData());

        } catch (Exception e) {
            e.printStackTrace();
            // 发生任何异常均返回失败视图
            outputBoundary.prepareFailView(new ExportReportOutputData());
        }
    }

    /**
     * 将 Markdown 字符串转换并保存为 PDF 文件
     * @param markdownContent Markdown 源码
     * @param fileName 带后缀的文件名
     * @throws Exception 将异常抛给上层统一处理
     */
    public void exportMarkdownToPdf(String markdownContent, String fileName) throws Exception {
        // 1. 准备输出目录
        Path downloadDir = Paths.get(Constants.DOWNLOAD_PATH);
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
        }
        String outputPath = downloadDir.resolve(fileName).toString();

        // 2. Markdown 转换为 HTML
        MutableDataSet options = new MutableDataSet();
        // 如果需要表格支持，可以在此添加 TablesExtension
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String htmlBody = renderer.render(parser.parse(markdownContent));

        // 3. 构造标准的 HTML 结构 (解决中文字体和编码问题)
        // 注意：PDF 渲染对 HTML 规范要求较高，建议添加基础样式
        String fullHtml = "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Arial Unicode MS', 'SimSun', sans-serif; padding: 20px; line-height: 1.6; }" +
                "pre { background: #f4f4f4; padding: 10px; border-radius: 5px; }" +
                "code { font-family: monospace; }" +
                "</style></head><body>" +
                htmlBody +
                "</body></html>";

        // 4. 将 HTML 渲染为 PDF
        try (OutputStream os = new FileOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // 必须指定字符集编码，否则中文可能乱码
            builder.withHtmlContent(fullHtml, null);
            builder.toStream(os);
            builder.run();
        }

        System.out.println("PDF 报表已生成: " + outputPath);
    }
}

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

        Report report = dao.getReportByReportId(reportId);

        try {
            exportMarkdownToPdf(report.getContent(), "report_" + UUID.randomUUID().toString());
        }
        catch (Exception e) {
            outputBoundary.prepareFailView(new ExportReportOutputData());
        }

        outputBoundary.prepareSuccessView(new ExportReportOutputData());
    }

    public void exportMarkdownToPdf(String markdownContent, String fileName) {
        String outputPath = Paths.get(Constants.DOWNLOAD_PATH, fileName).toString();

        try {
            // 1. 将 Markdown 转换为 HTML
            MutableDataSet options = new MutableDataSet();
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            String htmlContent = renderer.render(parser.parse(markdownContent));

            // 为了让 PDF 渲染器正常工作，需要包装成标准的 HTML 结构
            String fullHtml = "<html><body style='font-family: Arial, sans-serif;'>" + htmlContent + "</body></html>";

            // 2. 将 HTML 转换为 PDF
            try (OutputStream os = new FileOutputStream(outputPath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(fullHtml, null);
                builder.toStream(os);
                builder.run();
            }

            System.out.println("PDF 已成功导出至: " + outputPath);

        } catch (Exception e) {
            System.err.println("导出 PDF 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package use_case.export_report;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import entities.Report;
import org.jsoup.Jsoup;
import use_case.Constants;
import use_case.dto.ExportReportInputData;
import use_case.dto.ExportReportOutputData;

import org.jsoup.nodes.Document;
import java.io.*;
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
        Path downloadDir = Paths.get(Constants.DOWNLOAD_PATH);
        if (!Files.exists(downloadDir)) Files.createDirectories(downloadDir);
        String outputPath = downloadDir.resolve(fileName).toString();

        // 1. Markdown -> HTML
        MutableDataSet options = new MutableDataSet();
        String htmlBody = HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdownContent));

        // 2. 构造 HTML (注意：font-family 必须是没有任何引号的纯单词，或严格匹配)
        String fullHtml = """
            <!DOCTYPE html>
            <html lang="zh">
            <head>
                <meta charset="UTF-8" />
                <style>
                    /* 暴力覆盖：强制所有元素使用我们注册的名为 MyCustomFont 的字体 */
                    * { 
                        font-family: MyCustomFont !important; 
                        -fs-pdf-font-embed: embed;
                        -fs-pdf-font-encoding: Identity-H;
                    }
                    body { padding: 30px; font-size: 14px; }
                </style>
            </head>
            <body>
                %s
            </body>
            </html>
            """.formatted(htmlBody);

        // 3. Jsoup 规范化 (防止标签未闭合导致文件损坏)
        org.jsoup.nodes.Document doc = Jsoup.parse(fullHtml, "UTF-8");
        doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        String xhtml = doc.html();

        // 4. 读取字体 (确保是真正的 .ttf 文件，不是改名的 .ttc)
        byte[] fontBytes;
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("fonts/simsun.ttf")) {
            if (is == null) throw new FileNotFoundException("未找到字体文件");
            fontBytes = is.readAllBytes();
        }

        // 5. 渲染 PDF (核心：关闭 FastMode，手动指定编码)
        try (OutputStream os = new FileOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    return this.getClass().getClassLoader().getResourceAsStream("fonts/simsun.ttf");
                }
            }, "MyCustomFont", 400, BaseRendererBuilder.FontStyle.NORMAL, true);

            // 关键点 B: 不要使用 builder.useFastMode(); (它有时会导致编码映射失效)

            builder.withHtmlContent(xhtml, new File(".").toURI().toURL().toString());
            builder.toStream(os);
            builder.run();
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

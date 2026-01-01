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

import use_case.Constants;
import use_case.util.FileUtil;

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
            FileUtil.convertMarkdownToPdf(report.getContent(), Constants.DOWNLOAD_PATH + "/" + fileName);

            // 4. 只有执行成功才进入成功视图
            outputBoundary.prepareSuccessView(new ExportReportOutputData());

        } catch (Exception e) {
            e.printStackTrace();
            // 发生任何异常均返回失败视图
            outputBoundary.prepareFailView(new ExportReportOutputData());
        }
    }
}

package interface_adapter.export_report;

import use_case.dto.ExportReportInputData;
import use_case.export_report.ExportReportInputBoundary;

public class ExportReportController {

    private final ExportReportInputBoundary inputBoundary;

    public ExportReportController(ExportReportInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String reportId) {
        inputBoundary.execute(new ExportReportInputData(reportId));
    }
}

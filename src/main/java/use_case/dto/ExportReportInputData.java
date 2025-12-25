package use_case.dto;

public class ExportReportInputData {

    private final String reportId;

    public ExportReportInputData(String reportId) {
        this.reportId = reportId;
    }

    public String getReportId() {
        return reportId;
    }
}

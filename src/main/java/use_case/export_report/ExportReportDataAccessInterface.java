package use_case.export_report;

import entities.Report;

public interface ExportReportDataAccessInterface {

    Report getReportByReportId(String reportId);

}

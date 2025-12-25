package use_case.export_report;

import use_case.dto.ExportReportInputData;

public interface ExportReportInputBoundary {

    void execute(ExportReportInputData inputData);
}

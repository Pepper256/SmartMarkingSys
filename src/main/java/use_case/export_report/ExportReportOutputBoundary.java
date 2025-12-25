package use_case.export_report;

import use_case.dto.ExportReportOutputData;

public interface ExportReportOutputBoundary {

    void prepareSuccessView(ExportReportOutputData outputData);

    void prepareFailView(ExportReportOutputData outputData);
}

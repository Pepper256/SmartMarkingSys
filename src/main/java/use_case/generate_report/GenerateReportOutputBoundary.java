package use_case.generate_report;

import use_case.dto.GenerateReportOutputData;

public interface GenerateReportOutputBoundary {

    void prepareSuccessView(GenerateReportOutputData outputData);

    void prepareFailView(GenerateReportOutputData outputData);
}

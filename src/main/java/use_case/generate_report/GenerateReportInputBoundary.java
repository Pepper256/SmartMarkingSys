package use_case.generate_report;

import use_case.dto.GenerateReportInputData;

public interface GenerateReportInputBoundary {

    void execute(GenerateReportInputData inputData);
}

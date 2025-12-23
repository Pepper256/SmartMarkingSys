package interface_adapter.generate_report;

import use_case.dto.GenerateReportInputData;
import use_case.generate_report.GenerateReportInputBoundary;

public class GenerateReportController {

    private final GenerateReportInputBoundary inputBoundary;

    public GenerateReportController(GenerateReportInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String examId) {
        inputBoundary.execute(new GenerateReportInputData(examId));
    }
}

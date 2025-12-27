package interface_adapter.generate_report;

import use_case.dto.GenerateReportOutputData;
import use_case.generate_report.GenerateReportOutputBoundary;

public class GenerateReportPresenter implements GenerateReportOutputBoundary {
    @Override
    public void prepareSuccessView(GenerateReportOutputData outputData) {
        System.out.println("generate report successfully");
    }

    @Override
    public void prepareFailView(GenerateReportOutputData outputData) {
        System.out.println("generate report failed");
    }
}

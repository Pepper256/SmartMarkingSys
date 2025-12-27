package use_case.generate_report;

import use_case.dto.GenerateReportInputData;
import use_case.dto.GenerateReportOutputData;

public class GenerateReportUseCase implements GenerateReportInputBoundary{

    private final GenerateReportOutputBoundary outputBoundary;
    private final GenerateReportDataAccessInterface dao;

    public GenerateReportUseCase(GenerateReportOutputBoundary outputBoundary,
                                 GenerateReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(GenerateReportInputData inputData) {
        String examPaperId = inputData.getExamPaperId();
    }
}

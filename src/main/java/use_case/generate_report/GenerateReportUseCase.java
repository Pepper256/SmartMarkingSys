package use_case.generate_report;

import entities.MarkedStudentPaper;
import use_case.dto.GenerateReportInputData;

import java.util.List;

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

        List<MarkedStudentPaper> markedPapers = dao.getMarkedStudentPapersByExamPaperId(examPaperId);
    }
}

package interface_adapter.generate_student_report;

import use_case.dto.GenerateStudentReportOutputData;
import use_case.generate_student_report.GenerateStudentReportOutputBoundary;

public class GenerateStudentReportPresenter implements GenerateStudentReportOutputBoundary {


    @Override
    public void prepareSuccessView(GenerateStudentReportOutputData outputData) {
        System.out.println("generate success");
    }

    @Override
    public void prepareFailView(GenerateStudentReportOutputData outputData) {
        System.out.println("generate failed");
    }
}

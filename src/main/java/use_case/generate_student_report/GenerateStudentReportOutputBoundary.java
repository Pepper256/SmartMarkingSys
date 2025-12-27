package use_case.generate_student_report;

import use_case.dto.GenerateStudentReportOutputData;

public interface GenerateStudentReportOutputBoundary {

    void prepareSuccessView(GenerateStudentReportOutputData outputData);

    void prepareFailView(GenerateStudentReportOutputData outputData);
}

package use_case.generate_student_report;

import use_case.dto.GenerateStudentReportInputData;

public interface GenerateStudentReportInputBoundary {

    void execute(GenerateStudentReportInputData inputData);
}

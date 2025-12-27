package interface_adapter.generate_student_report;

import use_case.dto.GenerateReportInputData;
import use_case.dto.GenerateStudentReportInputData;
import use_case.generate_student_report.GenerateStudentReportInputBoundary;

public class GenerateStudentReportController {

    private final GenerateStudentReportInputBoundary inputBoundary;

    public GenerateStudentReportController(GenerateStudentReportInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(String markedStudentPaperId) {
        inputBoundary.execute(new GenerateStudentReportInputData(markedStudentPaperId));
    }
}

package use_case.dto;

public class GenerateStudentReportInputData {

    private final String markedStudentPaperId;

    public GenerateStudentReportInputData(String markedStudentPaperId) {
        this.markedStudentPaperId = markedStudentPaperId;
    }

    public String getMarkedStudentPaperId() {
        return markedStudentPaperId;
    }
}

package use_case.dto;

public class GenerateReportInputData {

    private final String examPaperId;

    public GenerateReportInputData(String examPaperId) {
        this.examPaperId = examPaperId;
    }

    public String getExamPaperId() {
        return examPaperId;
    }
}

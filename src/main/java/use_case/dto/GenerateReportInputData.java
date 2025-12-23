package use_case.dto;

public class GenerateReportInputData {

    private final String examPaperId;

    public GenerateReportInputData(String examId) {
        this.examPaperId = examId;
    }

    public String getExamPaperId() {
        return examPaperId;
    }
}

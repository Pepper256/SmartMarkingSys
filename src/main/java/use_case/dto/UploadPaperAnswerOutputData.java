package use_case.dto;

public class UploadPaperAnswerOutputData {

    private final String examId;
    private final String answerId;

    public UploadPaperAnswerOutputData(String examId, String answerId) {
        this.examId = examId;
        this.answerId = answerId;
    }

    public String getAnswerId() {
        return answerId;
    }

    public String getExamId() {
        return examId;
    }
}

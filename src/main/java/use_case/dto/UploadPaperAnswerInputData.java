package use_case.dto;

public class UploadPaperAnswerInputData {

    private final String examFilePath;
    private final String answerFilePath;

    public UploadPaperAnswerInputData(String examFilePath, String answerFilePath) {
        this.examFilePath = examFilePath;
        this.answerFilePath = answerFilePath;
    }

    public String getAnswerFilePath() {
        return answerFilePath;
    }

    public String getExamFilePath() {
        return examFilePath;
    }
}

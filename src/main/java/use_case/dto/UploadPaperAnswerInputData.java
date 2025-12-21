package use_case.dto;

public class UploadPaperAnswerInputData {

    private final String filePath;

    public UploadPaperAnswerInputData(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}

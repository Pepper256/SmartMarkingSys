package interface_adapter.upload_paper_answer;

import use_case.dto.UploadPaperAnswerInputData;
import use_case.upload_paper_answer.UploadPaperAnswerInputBoundary;

public class UploadPaperAnswerController {

    private final UploadPaperAnswerInputBoundary uploadPaperAnswerInputBoundary;

    public UploadPaperAnswerController(UploadPaperAnswerInputBoundary uploadPaperAnswerInputBoundary) {
        this.uploadPaperAnswerInputBoundary = uploadPaperAnswerInputBoundary;
    }

    void execute(String examFilePath, String answerFilePath) {
        uploadPaperAnswerInputBoundary.execute(new UploadPaperAnswerInputData(examFilePath, answerFilePath));
    }
}

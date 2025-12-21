package interface_adapter.upload_paper_answer;

import use_case.dto.UploadPaperAnswerOutputData;
import use_case.upload_paper_answer.UploadPaperAnswerOutputBoundary;

public class UploadPaperAnswerPresenter implements UploadPaperAnswerOutputBoundary {
    @Override
    public void prepareSuccessView(UploadPaperAnswerOutputData outputData) {
        System.out.println("upload success");
    }

    @Override
    public void prepareFailView(UploadPaperAnswerOutputData outputData) {
        System.out.println("upload failed");
    }
}

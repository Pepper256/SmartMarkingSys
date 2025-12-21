package use_case.upload_paper_answer;

import use_case.dto.UploadPaperAnswerOutputData;

public interface UploadPaperAnswerOutputBoundary {

    void prepareSuccessView(UploadPaperAnswerOutputData outputData);

    void prepareFailView(UploadPaperAnswerOutputData outputData);
}

package use_case.upload_paper_answer;

import use_case.dto.UploadPaperAnswerInputData;

public interface UploadPaperAnswerInputBoundary {

    void execute(UploadPaperAnswerInputData inputData);
}

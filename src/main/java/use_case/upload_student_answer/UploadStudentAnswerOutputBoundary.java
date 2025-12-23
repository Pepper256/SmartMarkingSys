package use_case.upload_student_answer;

import use_case.dto.UploadStudentAnswerOutputData;

public interface UploadStudentAnswerOutputBoundary {

    void prepareSuccessView(UploadStudentAnswerOutputData outputData);

    void prepareFailView(UploadStudentAnswerOutputData outputData);
}

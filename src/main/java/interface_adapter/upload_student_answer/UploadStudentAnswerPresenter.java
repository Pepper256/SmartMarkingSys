package interface_adapter.upload_student_answer;

import use_case.dto.UploadStudentAnswerOutputData;
import use_case.upload_student_answer.UploadStudentAnswerOutputBoundary;

public class UploadStudentAnswerPresenter implements UploadStudentAnswerOutputBoundary {
    @Override
    public void prepareSuccessView(UploadStudentAnswerOutputData outputData) {
        System.out.println("upload success");
    }

    @Override
    public void prepareFailView(UploadStudentAnswerOutputData outputData) {
        System.out.println("upload failed");
    }
}

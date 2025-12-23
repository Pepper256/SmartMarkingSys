package use_case.upload_student_answer;

import use_case.dto.UploadStudentAnswerInputData;

import java.util.List;

public class UploadStudentAnswerUseCase implements UploadStudentAnswerInputBoundary{

    private final UploadStudentAnswerDataAccessInterface dao;

    public UploadStudentAnswerUseCase(UploadStudentAnswerDataAccessInterface dao) {
        this.dao = dao;
    }

    @Override
    public void execute(UploadStudentAnswerInputData inputData) {
        List<String> paths = inputData.getPaths();
    }
}

package use_case.upload_student_answer;

import use_case.dto.UploadStudentAnswerInputData;

public interface UploadStudentAnswerInputBoundary {

    void execute(UploadStudentAnswerInputData inputData);
}

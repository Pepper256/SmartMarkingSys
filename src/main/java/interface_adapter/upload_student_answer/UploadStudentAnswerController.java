package interface_adapter.upload_student_answer;

import use_case.dto.UploadStudentAnswerInputData;
import use_case.upload_student_answer.UploadStudentAnswerInputBoundary;

import java.util.List;

public class UploadStudentAnswerController {

    private final UploadStudentAnswerInputBoundary inputBoundary;

    public UploadStudentAnswerController(UploadStudentAnswerInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(List<String> paths, String examPaperId) {
        inputBoundary.execute(new UploadStudentAnswerInputData(paths, examPaperId));
    }
}

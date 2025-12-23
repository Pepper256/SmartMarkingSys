package use_case.dto;

import java.util.List;

public class UploadStudentAnswerInputData {

    private final List<String> paths;
    private final String examPaperId;

    public UploadStudentAnswerInputData(List<String> paths, String examPaperId) {
        this.examPaperId = examPaperId;
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }

    public String getExamPaperId() {
        return examPaperId;
    }
}

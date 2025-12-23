package use_case.dto;

import java.util.List;

public class UploadStudentAnswerInputData {

    private final List<String> paths;

    public UploadStudentAnswerInputData(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }
}

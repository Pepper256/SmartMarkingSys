package use_case.dto;

import java.util.List;

public class AutoMarkingInputData {

    private final List<String> studentPaperIds;

    public AutoMarkingInputData(List<String> studentPaperIds) {
        this.studentPaperIds = studentPaperIds;
    }

    public List<String> getStudentPaperIds() {
        return studentPaperIds;
    }
}

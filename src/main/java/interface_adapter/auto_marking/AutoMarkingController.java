package interface_adapter.auto_marking;

import use_case.auto_marking.AutoMarkingInputBoundary;
import use_case.dto.AutoMarkingInputData;

import java.util.List;

public class AutoMarkingController {

    private final AutoMarkingInputBoundary inputBoundary;

    public AutoMarkingController(AutoMarkingInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    void execute(List<String> studentPaperIds) {
        inputBoundary.execute(new AutoMarkingInputData(studentPaperIds));
    }
}

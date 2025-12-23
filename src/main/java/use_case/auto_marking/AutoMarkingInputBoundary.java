package use_case.auto_marking;

import use_case.dto.AutoMarkingInputData;

public interface AutoMarkingInputBoundary {

    void execute(AutoMarkingInputData inputData);
}

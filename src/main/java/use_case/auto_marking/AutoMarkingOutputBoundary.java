package use_case.auto_marking;

import use_case.dto.AutoMarkingOutputData;

public interface AutoMarkingOutputBoundary {

    void prepareSuccessView(AutoMarkingOutputData outputData);

    void prepareFailView(AutoMarkingOutputData outputData);
}

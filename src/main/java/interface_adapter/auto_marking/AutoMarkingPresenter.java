package interface_adapter.auto_marking;

import use_case.auto_marking.AutoMarkingOutputBoundary;
import use_case.dto.AutoMarkingOutputData;

public class AutoMarkingPresenter implements AutoMarkingOutputBoundary {


    @Override
    public void prepareSuccessView(AutoMarkingOutputData outputData) {
        System.out.println("marking success");
    }

    @Override
    public void prepareFailView(AutoMarkingOutputData outputData) {
        System.out.println("marking failed");
    }
}

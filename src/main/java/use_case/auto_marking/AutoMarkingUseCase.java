package use_case.auto_marking;

import use_case.dto.AutoMarkingInputData;

import java.util.List;

public class AutoMarkingUseCase implements AutoMarkingInputBoundary{

    private final AutoMarkingOutputBoundary outputBoundary;
    private final AutoMarkingDataAccessInterface dao;

    public AutoMarkingUseCase(AutoMarkingOutputBoundary outputBoundary,
                              AutoMarkingDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
    }

    @Override
    public void execute(AutoMarkingInputData inputData) {
        List<String> ids = inputData.getStudentPaperIds();

        for (String id : ids) {

        }
    }
}

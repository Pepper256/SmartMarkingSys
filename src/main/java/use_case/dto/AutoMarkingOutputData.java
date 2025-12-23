package use_case.dto;

import java.util.List;

public class AutoMarkingOutputData {

    private final List<String> markedPapers;

    public AutoMarkingOutputData(List<String> markedPapers) {
        this.markedPapers = markedPapers;
    }

    public List<String> getMarkedPapers() {
        return markedPapers;
    }
}

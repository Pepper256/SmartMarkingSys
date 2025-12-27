package use_case.dto;

public class GenerateReportOutputData {

    private final String content;

    public GenerateReportOutputData(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

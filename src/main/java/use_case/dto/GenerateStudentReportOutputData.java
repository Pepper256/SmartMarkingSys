package use_case.dto;

public class GenerateStudentReportOutputData {

    private final String markdownContent;

    public GenerateStudentReportOutputData(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }
}

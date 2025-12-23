package entities;

public class Report {

    private String id;
    private String examPaperId;
    private String content;

    public Report(String id, String examPaperId, String content) {
        this.id = id;
        this.examPaperId = examPaperId;
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setExamPaperId(String examPaperId) {
        this.examPaperId = examPaperId;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getExamPaperId() {
        return examPaperId;
    }
}

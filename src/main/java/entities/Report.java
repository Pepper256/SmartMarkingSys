package entities;

import com.alibaba.fastjson.JSONObject;

public class Report {

    private String id;
    private String examPaperId;
    private String studentPaperId;
    private String content;

    public Report(String id, String examPaperId, String studentPaperId, String content) {
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

    public String getStudentPaperId() {
        return studentPaperId;
    }

    public void setStudentPaperId(String studentPaperId) {
        this.studentPaperId = studentPaperId;
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.id);
        jsonObject.put("examPaperId", this.examPaperId);
        jsonObject.put("content", this.content);
        jsonObject.put("studentPaperId", this.studentPaperId);
        return jsonObject.toJSONString().trim();
    }
}

package entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class StudentPaper {

    private String id;
    private String examPaperId;
    private HashMap<String, String> questions;
    private HashMap<String, String> responses; // map questions to student responses
    private String subject;
    private String coordContent;

    public StudentPaper() {}

    public StudentPaper(String id,
                        String examPaperId,
                        String subject,
                        HashMap<String, String> questions,
                        HashMap<String, String> responses,
                        String coordContent) {
        this.id = id;
        this.examPaperId = examPaperId;
        this.questions = questions;
        this.responses = responses;
        this.subject = subject;
        this.coordContent = coordContent;
    }

    public HashMap<String, String> getQuestions() {
        return questions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setQuestions(HashMap<String, String> questions) {
        this.questions = questions;
    }

    public void setResponses(HashMap<String, String> responses) {
        this.responses = responses;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public HashMap<String, String> getResponses() {
        return responses;
    }

    public String getCoordContent() {
        return coordContent;
    }

    public void setCoordContent(String coordContent) {
        this.coordContent = coordContent;
    }

    public String getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(String examPaperId) {
        this.examPaperId = examPaperId;
    }

    /**
     * @param json 需要转化的json字符串，要求包含字段id，subject，questions，responses
     * @return 转换以后的StudentPaper对象，储存信息为json中的信息，如果转换失败则返回null
     */
    public static StudentPaper jsonToStudentPaper(String json){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, StudentPaper.class);
        }
        catch (Exception e){
            return null;
        }
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("examId", this.getExamPaperId());
        jsonObject.put("subject", this.getSubject());
        jsonObject.put("questions", this.getQuestions());
        jsonObject.put("responses", this.getResponses());
        jsonObject.put("coordContent", this.getCoordContent());
        return jsonObject.toJSONString();
    }

    @Override
    public boolean equals(Object obj) {

    }
}

package entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class ExamPaper {

    private String id;
    private String subject;
    private HashMap<String, String> questions;
    private String answerId;

    public ExamPaper() {}

    public ExamPaper(String id, String subject, HashMap<String, String> questions, String answerId) {
        this.id = id;
        this.subject = subject;
        this.questions = questions;
        this.answerId = answerId;
    }

    /**
     * @param json 需要转化的json字符串，要求包含字段id，subject，questions，answerId
     * @return 转换以后的ExamPaper对象，储存信息为json中的信息，如果转换失败则返回null
     */
    public static ExamPaper jsonToExamPaper(String json){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, ExamPaper.class);
        }
        catch (Exception e){
            return null;
        }
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getId());
        jsonObject.put("subject", this.getSubject());
        jsonObject.put("questions", this.getQuestions());
        jsonObject.put("answerId", this.getAnswerId());
        return jsonObject.toJSONString().trim();
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public HashMap<String, String> getQuestions() {
        return questions;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setQuestions(HashMap<String, String> questions) {
        this.questions = questions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ExamPaper examPaperObj = (ExamPaper) obj;

            return examPaperObj.getId().equals(this.id) &&
                    examPaperObj.getQuestions().equals(this.questions) &&
                    examPaperObj.getSubject().equals(this.subject) &&
                    examPaperObj.getAnswerId().equals(this.getAnswerId());
        }
        catch (Exception e) {
            return false;
        }
    }
}

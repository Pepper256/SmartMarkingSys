package entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class AnswerPaper{

    private String id;
    private String subject;
    private HashMap<String, String> questions;
    private HashMap<String, String> answers; // map questions to answers

    public AnswerPaper(String id, String subject, HashMap<String, String> questions, HashMap<String, String> answers) {
        this.id = id;
        this.subject = subject;
        this.questions = questions;
        this.answers = answers;
    }

    /**
     * @param json 需要转化的json字符串，要求包含字段id，subject，questions，answers
     * @return 转换以后的AnswerPaper对象，储存信息为json中的信息，如果转换失败则返回null
     */
    public static AnswerPaper jsonToAnswerPaper(String json){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, AnswerPaper.class);
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
        jsonObject.put("answers", this.getAnswers());
        return jsonObject.toJSONString();
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public HashMap<String, String> getAnswers() {
        return answers;
    }

    public HashMap<String, String> getQuestions() {
        return questions;
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

    public void setAnswers(HashMap<String, String> answers) {
        this.answers = answers;
    }
}

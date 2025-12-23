package entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class ExamPaper {

    private final String id;
    private final String subject;
    private final HashMap<String, String> questions;
    private final String answerId;

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
        return jsonObject.toJSONString();
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
}

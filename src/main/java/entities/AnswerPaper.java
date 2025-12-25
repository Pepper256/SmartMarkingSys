package entities;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class AnswerPaper{

    private String id;
    private String examPaperId;
    private String subject;
    private HashMap<String, String> questions;
    private HashMap<String, String> answers; // map questions to answers

    public AnswerPaper() {}

    public AnswerPaper(String id,
                       String examPaperId,
                       String subject,
                       HashMap<String, String> questions,
                       HashMap<String, String> answers) {
        this.id = id;
        this.examPaperId = examPaperId;
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
        jsonObject.put("examPaperId", this.getExamPaperId());
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

    public String getExamPaperId() {
        return examPaperId;
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

    public void setExamPaperId(String examPaperId) {
        this.examPaperId = examPaperId;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            AnswerPaper answerPaperObj = (AnswerPaper) obj;

            return answerPaperObj.getId().equals(this.id) &&
                    answerPaperObj.getQuestions().equals(this.questions) &&
                    answerPaperObj.getSubject().equals(this.subject) &&
                    answerPaperObj.getExamPaperId().equals(this.examPaperId) &&
                    answerPaperObj.getAnswers().equals(this.answers);
        }
        catch (Exception e) {
            return false;
        }
    }
}

package entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class AnswerPaper{

    private final String id;
    private final String subject;
    private final HashMap<String, String> questions;
    private final HashMap<String, String> answers; // map questions to answers

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

    /**
     * @param answerPaper 需要转化的AnswerPaper对象
     * @return 转换以后的json字符串，储存信息为原来ExamPaper中的信息，如果转换失败则返回null
     */
    public static String answerPaperToJson(AnswerPaper answerPaper) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(answerPaper);
        }
        catch (Exception e) {
            return null;
        }
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
}

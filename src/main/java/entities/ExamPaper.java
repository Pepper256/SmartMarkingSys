package entities;

import java.util.HashMap;
import java.util.List;

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

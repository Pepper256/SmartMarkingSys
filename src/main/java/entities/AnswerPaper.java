package entities;

import java.util.HashMap;
import java.util.List;

public class AnswerPaper{

    private final String id;
    private final String subject;
    private final List<String> questions;
    private final HashMap<String, String> answers; // map questions to answers

    public AnswerPaper(String id, String subject, List<String> questions, HashMap<String, String> answers) {
        this.id = id;
        this.subject = subject;
        this.questions = questions;
        this.answers = answers;
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

    public List<String> getQuestions() {
        return questions;
    }
}

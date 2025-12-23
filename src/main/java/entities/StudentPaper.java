package entities;

import java.util.HashMap;
import java.util.List;

public class StudentPaper {

    private final String id;
    private final HashMap<String, String> questions;
    private final HashMap<String, String> responses; // map questions to student responses
    private final String subject;

    public StudentPaper(String id,
                        String subject,
                        HashMap<String, String> questions,
                        HashMap<String, String> responses) {
        this.id = id;
        this.questions = questions;
        this.responses = responses;
        this.subject = subject;
    }

    public HashMap<String, String> getQuestions() {
        return questions;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public HashMap<String, String> getResponses() {
        return responses;
    }
}

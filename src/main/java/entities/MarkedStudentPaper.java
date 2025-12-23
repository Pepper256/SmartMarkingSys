package entities;

import jdk.vm.ci.code.site.Mark;

import java.util.HashMap;

public class MarkedStudentPaper extends StudentPaper{

    private String markedContent;
    private HashMap<String, Boolean> correctness;
    private HashMap<String, String> reasons;

    public MarkedStudentPaper(String id,
                                String subject,
                                HashMap<String, String> questions,
                                HashMap<String, String> responses,
                                String coordContent,
                                HashMap<String, Boolean> correctness,
                                String markedContent,
                              HashMap<String, String> reasons) {
        super(id, subject, questions, responses, coordContent);
        this.correctness = correctness;
        this.markedContent = markedContent;
    }

    public MarkedStudentPaper(StudentPaper studentPaper,
                              HashMap<String, Boolean> correctness,
                              String markedContent,
                              HashMap<String, String> reasons) {
        super(studentPaper.getId(),
                studentPaper.getSubject(),
                studentPaper.getQuestions(),
                studentPaper.getResponses(),
                studentPaper.getCoordContent());
        this.correctness = correctness;
        this.markedContent = markedContent;
        this.reasons = reasons;
    }

    public String getMarkedContent() {
        return markedContent;
    }

    public void setMarkedContent(String markedContent) {
        this.markedContent = markedContent;
    }

    public HashMap<String, Boolean> getCorrectness() {
        return correctness;
    }

    public void setCorrectness(HashMap<String, Boolean> correctness) {
        this.correctness = correctness;
    }

    public HashMap<String, String> getReasons() {
        return reasons;
    }

    public void setReasons(HashMap<String, String> reasons) {
        this.reasons = reasons;
    }
}

package entities;

import jdk.vm.ci.code.site.Mark;

import java.util.HashMap;

public class MarkedStudentPaper extends StudentPaper{

    private String markedContent;

    public MarkedStudentPaper(String id,
                                String subject,
                                HashMap<String, String> questions,
                                HashMap<String, String> responses,
                                String coordContent,
                                String markedContent) {
        super(id, subject, questions, responses, coordContent);
        this.markedContent = markedContent;
    }

    public MarkedStudentPaper(StudentPaper studentPaper, String markedContent) {
        super(studentPaper.getId(),
                studentPaper.getSubject(),
                studentPaper.getQuestions(),
                studentPaper.getResponses(),
                studentPaper.getCoordContent());
        this.markedContent = markedContent;
    }

    public String getMarkedContent() {
        return markedContent;
    }

    public void setMarkedContent(String markedContent) {
        this.markedContent = markedContent;
    }
}

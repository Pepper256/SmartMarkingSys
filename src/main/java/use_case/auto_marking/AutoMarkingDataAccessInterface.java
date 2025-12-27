package use_case.auto_marking;

import entities.AnswerPaper;
import entities.MarkedStudentPaper;
import entities.StudentPaper;

import java.util.List;

public interface AutoMarkingDataAccessInterface {

    StudentPaper getStudentPaperById(String id);

    void storeMarkedPapers(List<MarkedStudentPaper> studentPapers);

    AnswerPaper getAnswerPaperByExamPaperId(String examPaperId);
}

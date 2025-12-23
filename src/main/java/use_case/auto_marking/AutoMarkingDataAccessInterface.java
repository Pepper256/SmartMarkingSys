package use_case.auto_marking;

import entities.StudentPaper;

public interface AutoMarkingDataAccessInterface {

    StudentPaper getStudentPaperById(String id);

}

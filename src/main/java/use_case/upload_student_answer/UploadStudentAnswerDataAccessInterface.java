package use_case.upload_student_answer;

import entities.ExamPaper;
import entities.StudentPaper;

import java.util.List;

public interface UploadStudentAnswerDataAccessInterface {

    void saveStudentPapers(List<StudentPaper> studentPapers);

    ExamPaper getExamPaperById(String examPaperId);
}

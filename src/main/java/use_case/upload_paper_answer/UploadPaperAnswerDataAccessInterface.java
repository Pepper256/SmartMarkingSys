package use_case.upload_paper_answer;

import entities.ExamPaper;
import entities.AnswerPaper;

public interface UploadPaperAnswerDataAccessInterface {

    void storeExamAnswer(ExamPaper examPaper, AnswerPaper answerPaper);
}

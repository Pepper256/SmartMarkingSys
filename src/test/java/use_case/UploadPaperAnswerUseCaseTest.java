package use_case;

import app.Main;
import entities.AnswerPaper;
import entities.ExamPaper;
import interface_adapter.upload_paper_answer.UploadPaperAnswerPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.dto.UploadPaperAnswerInputData;
import use_case.upload_paper_answer.UploadPaperAnswerDataAccessInterface;
import use_case.upload_paper_answer.UploadPaperAnswerUseCase;

public class UploadPaperAnswerUseCaseTest {

    private String apikey;
    private UploadPaperAnswerUseCase useCase;
    private final UploadPaperAnswerPresenter presenter = new UploadPaperAnswerPresenter();
    private final MockDao dao = new MockDao();

    @BeforeEach
    void setup() {
        apikey = Main.loadQwenApiKey();
        useCase = new UploadPaperAnswerUseCase(presenter, dao);

    }

    @Test
    public void UploadPaperAnswerUseCaseApiTest() {
        useCase.execute(new UploadPaperAnswerInputData("src/main/resources/test.pdf",
                "src/main/resources/test.pdf"));
    }

    private class MockDao implements UploadPaperAnswerDataAccessInterface {

        @Override
        public void storeExamAnswer(ExamPaper examPaper, AnswerPaper answerPaper) {

            System.out.println(examPaper.toJsonString());
            System.out.println(answerPaper.toJsonString());
            System.out.println("stored");
        }
    }
}

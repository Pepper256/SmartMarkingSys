package use_case;

import entities.StudentPaper;
import interface_adapter.upload_student_answer.UploadStudentAnswerPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import use_case.dto.UploadStudentAnswerInputData;
import use_case.upload_student_answer.UploadStudentAnswerDataAccessInterface;
import use_case.upload_student_answer.UploadStudentAnswerInputBoundary;
import use_case.upload_student_answer.UploadStudentAnswerUseCase;

import java.util.ArrayList;
import java.util.List;

public class UploadStudentAnswerUseCaseTest {

    private final UploadStudentAnswerUseCase inputBoundary = new UploadStudentAnswerUseCase(
            new MockDao(),
            new UploadStudentAnswerPresenter()
    );

    private final List<String> paths = new ArrayList<>();

    @BeforeEach
    void setup() {
        paths.add("./src/main/resources/test.pdf");
    }

    @Test
    @Disabled("Integration test: requires config.properties API keys and real PDF inputs")
    public void UploadStudentAnswerUseCaseApiTest() {
        inputBoundary.execute(new UploadStudentAnswerInputData(paths, "1"));
    }

    private static class MockDao implements UploadStudentAnswerDataAccessInterface {

        @Override
        public void saveStudentPapers(List<StudentPaper> studentPapers) {
            System.out.println(studentPapers.get(0).toJsonString());
        }
    }
}

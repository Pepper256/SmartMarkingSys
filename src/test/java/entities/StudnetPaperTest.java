package entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class StudnetPaperTest {

    private StudentPaper studentPaper;
    private String studentPaperJson;

    @BeforeEach
    void setup() {
        HashMap<String, String> questions, responses;
        questions = new HashMap<>();
        responses = new HashMap<>();
        questions.put("1", "1");
        responses.put("1", "1");

        studentPaper = new StudentPaper(
                "1",
                "1",
                "1",
                questions,
                responses,
                "1"
        );
        studentPaperJson = "{" +
                "\"id\":\"1\"," +
                "\"subject\":\"1\"," +
                "\"examPaperId\":\"1\"," +
                "\"questions\":{\"1\":\"1\"}," +
                "\"responses\":{\"1\":\"1\"}," +
                "\"coordContent\":\"1\"" +
                "}";
    }


}

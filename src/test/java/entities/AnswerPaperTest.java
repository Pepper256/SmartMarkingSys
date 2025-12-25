package entities;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnswerPaperTest {

    private AnswerPaper answerPaper;
    private String answerPaperJson;

    @BeforeEach
    void setup() {
        HashMap<String, String> questions = new HashMap<>();
        questions.put("1", "1");

        HashMap<String, String> answers = new HashMap<>();
        answers.put("1", "1");

        answerPaper = new AnswerPaper(
                "1",
                "1",
                "1",
                questions,
                answers
        );

        answerPaperJson = "{\"id\":\"1\",\"subject\":\"1\",\"examPaperId\":\"1\",\"questions\":{\"1\":\"1\"},\"answers\":{\"1\":\"1\"}}";

    }

    @Test
    public void AnswerPaperToJsonTest() {
        // Debug the contents
        System.out.println("String 1: " + answerPaper.toJsonString());
        System.out.println("String 2: " + answerPaperJson);

        Object obj1 = JSON.parseObject(answerPaper.toJsonString().trim());
        Object obj2 = JSON.parseObject(answerPaperJson.trim());

        assertEquals(obj1, obj2);
    }

    @Test
    public void jsonToAnswerPaperTest() {
        Object obj1 = answerPaper;
        Object obj2 = AnswerPaper.jsonToAnswerPaper(answerPaperJson);

        assertEquals(obj1, obj2);
    }
}

package entities;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExamPaperTest {

    private ExamPaper examPaper;
    private String examPaperJson;

    @BeforeEach
    void setup() {
        HashMap<String, String> questions = new HashMap<>();
        questions.put("1", "1");
        examPaper = new ExamPaper("1", "1", questions, "2");

        examPaperJson = "{\"id\":\"1\",\"subject\":\"1\",\"questions\":{\"1\":\"1\"},\"answerId\":\"2\"}".trim();
    }

    @Test
    public void examPaperToJsonTest() {
        // Debug the contents
        System.out.println("String 1: " + examPaper.toJsonString());
        System.out.println("String 2: " + examPaperJson);

        Object obj1 = JSON.parseObject(examPaper.toJsonString().trim());
        Object obj2 = JSON.parseObject(examPaperJson.trim());

        assertEquals(obj1, obj2);
    }

    @Test
    public void jsonToExamPaperTest() {
        Object obj1 = examPaper;
        Object obj2 = ExamPaper.jsonToExamPaper(examPaperJson);

        assertEquals(obj1, obj2);
    }
}

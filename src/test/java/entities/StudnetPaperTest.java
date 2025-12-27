package entities;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void studentPaperToJsonTest() {
        // Debug the contents
        System.out.println("String 1: " + studentPaper.toJsonString());
        System.out.println("String 2: " + studentPaperJson);

        Object obj1 = JSON.parseObject(studentPaper.toJsonString().trim());
        Object obj2 = JSON.parseObject(studentPaperJson.trim());

        assertEquals(obj1, obj2);
    }

    @Test
    public void jsonToStudentPaperTest() {
        Object obj1 = studentPaper;
        Object obj2 = StudentPaper.jsonToStudentPaper(studentPaperJson);

        assertEquals(obj1, obj2);
    }
}

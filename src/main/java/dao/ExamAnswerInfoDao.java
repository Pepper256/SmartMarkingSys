package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AnswerPaper;
import entities.ExamPaper;
import use_case.Constants;
import use_case.upload_paper_answer.UploadPaperAnswerDataAccessInterface;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExamAnswerInfoDao implements UploadPaperAnswerDataAccessInterface {

    private static class Database {
        private List<ExamPaper> examPapers;
        private List<AnswerPaper> answerPapers;

        public Database(List<ExamPaper> examPapers, List<AnswerPaper> answerPapers) {
            this.examPapers = examPapers;
            this.answerPapers = answerPapers;
        }

        public List<ExamPaper> getExamPapers() {
            return examPapers;
        }

        public void setExamPapers(List<ExamPaper> examPapers) {
            this.examPapers = examPapers;
        }

        public List<AnswerPaper> getAnswerPapers() {
            return answerPapers;
        }

        public void setAnswerPapers(List<AnswerPaper> answerPapers) {
            this.answerPapers = answerPapers;
        }

        public void addExamPaper(ExamPaper examPaper) {
            this.examPapers.add(examPaper);
        }

        public void addAnswerPaper(AnswerPaper answerPaper) {
            this.answerPapers.add(answerPaper);
        }
    }

    @Override
    public void storeExamAnswer(ExamPaper examPaper, AnswerPaper answerPaper) {
        String examJson = examPaper.toJsonString();
        String answerJson = answerPaper.toJsonString();

        String relativePath = Constants.DAO_PATH;
        String jsonString = null;

        ObjectMapper mapper = new ObjectMapper();

        try {

            Path path = Paths.get(relativePath);

            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Database db = mapper.readValue(new File(relativePath), Database.class);
            db.addAnswerPaper(answerPaper);
            db.addExamPaper(examPaper);
            jsonString = mapper.writeValueAsString(db);

            Files.write(path,
                    jsonString.getBytes(StandardCharsets.UTF_8));

            System.out.println("JSON 文件已成功保存至: " + path.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("保存文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

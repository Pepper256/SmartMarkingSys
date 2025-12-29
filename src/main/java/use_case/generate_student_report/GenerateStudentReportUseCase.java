package use_case.generate_student_report;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.MarkedStudentPaper;
import entities.Report;
import use_case.Constants;
import use_case.dto.GenerateStudentReportInputData;
import use_case.dto.GenerateStudentReportOutputData;
import use_case.util.ApiUtil;

import java.util.HashMap;
import java.util.UUID;

public class GenerateStudentReportUseCase implements GenerateStudentReportInputBoundary {

    private final GenerateStudentReportOutputBoundary outputBoundary;
    private final GenerateStudentReportDataAccessInterface dao;
    private final ObjectMapper objectMapper;

    public GenerateStudentReportUseCase(GenerateStudentReportOutputBoundary outputBoundary,
                                        GenerateStudentReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(GenerateStudentReportInputData inputData) {
        String markedStudentPaperId = inputData.getMarkedStudentPaperId();
        MarkedStudentPaper paper = dao.getMarkedStudentPaperById(markedStudentPaperId);

        if (paper == null) {
            outputBoundary.prepareFailView(new GenerateStudentReportOutputData(""));
            return;
        }

        try {
            // 1. 整理题目数据
            String questionDetails = formatQuestionDetails(paper);

            // 2. 组装最终 Prompt
            String finalPrompt = Constants.REPORT_PROMPT + "\n以下为试卷内容\n" + questionDetails;

            // 3. 发起原生 HTTP 请求调用 Qwen-VL-Flash
            String generatedReport = ApiUtil.callDeepseekApi(finalPrompt);

            // 4. 创建 Report 对象并持久化
            Report report = new Report(
                    "report_" + UUID.randomUUID().toString(),
                    paper.getExamPaperId(),
                    paper.getId(),
                    generatedReport
            );
            dao.storeReport(report);

            // 5. 调用成功视图
            GenerateStudentReportOutputData outputData = new GenerateStudentReportOutputData(generatedReport);
            outputBoundary.prepareSuccessView(outputData);

        } catch (Exception e) {
            // 捕获所有可能的网络、解析或逻辑异常
            outputBoundary.prepareFailView(new GenerateStudentReportOutputData(""));
        }
    }

    private String formatQuestionDetails(MarkedStudentPaper paper) {
        HashMap<String, String> questions = paper.getQuestions();
        HashMap<String, Boolean> correctness = paper.getCorrectness();
        HashMap<String, String> responses = paper.getResponses();

        JSONObject jsonObject = new JSONObject();
        for(String key : questions.keySet()) {
            JSONObject temp = new JSONObject();
            temp.put("question", questions.get(key));
            temp.put("correctness", correctness.getOrDefault(key, false));
            temp.put("response", responses.getOrDefault(key, ""));
            jsonObject.put(key, temp);
        }
        return jsonObject.toJSONString();
    }

}

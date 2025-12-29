package use_case.generate_report;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.MarkedStudentPaper;
import entities.Report;
import use_case.Constants;
import use_case.dto.GenerateReportInputData;
import use_case.dto.GenerateReportOutputData;
import use_case.util.ApiUtil;

import java.util.*;

public class GenerateReportUseCase implements GenerateReportInputBoundary{

    private final GenerateReportOutputBoundary outputBoundary;
    private final GenerateReportDataAccessInterface dao;
    private final ObjectMapper objectMapper;

    public GenerateReportUseCase(GenerateReportOutputBoundary outputBoundary,
                                 GenerateReportDataAccessInterface dao) {
        this.outputBoundary = outputBoundary;
        this.dao = dao;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void execute(GenerateReportInputData inputData) {
        String examPaperId = inputData.getExamPaperId();

        List<MarkedStudentPaper> markedStudentPapers = dao.getMarkedStudentPapersByExamPaperId(examPaperId);

        if(markedStudentPapers.isEmpty()) {
            outputBoundary.prepareFailView(new GenerateReportOutputData(""));
        }

        String finalPrompt;

        List<String> questionDetails = new ArrayList<>();

        for(MarkedStudentPaper paper : markedStudentPapers) {
            questionDetails.add(formatQuestionDetails(paper));
        }

        try {
            finalPrompt = Constants.REPORT_PROMPT + "\n以下本次考试学生的批改后的试卷内容\n" + objectMapper.writeValueAsString(questionDetails);

            // 3. 发起原生 HTTP 请求调用 Qwen-VL-Flash
            String generatedReport = ApiUtil.callDeepseekApi(finalPrompt);

            // 4. 创建 Report 对象并持久化
            Report report = new Report(
                    "report_" + UUID.randomUUID().toString(),
                    examPaperId,
                    null,
                    generatedReport
            );
            dao.storeReport(report);
        }
        catch (Exception e) {
            System.out.println("api调用失败");
            outputBoundary.prepareFailView(new GenerateReportOutputData(""));
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

package use_case.generate_report;

import entities.MarkedStudentPaper;
import entities.Report;

import java.util.List;

public interface GenerateReportDataAccessInterface {

    void storeReport(Report report);

    List<MarkedStudentPaper> getMarkedStudentPapersByExamPaperId(String examPaperId);
}

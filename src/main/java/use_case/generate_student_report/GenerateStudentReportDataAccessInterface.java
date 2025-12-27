package use_case.generate_student_report;

import entities.MarkedStudentPaper;
import entities.Report;

import java.util.List;

public interface GenerateStudentReportDataAccessInterface {

    void storeReport(Report report);

    MarkedStudentPaper getMarkedStudentPaperById(String markedStudentPaperId);
}

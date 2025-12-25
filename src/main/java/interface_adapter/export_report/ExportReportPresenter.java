package interface_adapter.export_report;

import use_case.dto.ExportReportOutputData;
import use_case.export_report.ExportReportOutputBoundary;

public class ExportReportPresenter implements ExportReportOutputBoundary {

    @Override
    public void prepareSuccessView(ExportReportOutputData outputData) {
        System.out.println("export success");
    }

    @Override
    public void prepareFailView(ExportReportOutputData outputData) {
        System.out.println("export success");
    }
}

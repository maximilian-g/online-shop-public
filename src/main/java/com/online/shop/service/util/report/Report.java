package com.online.shop.service.util.report;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private String reportName;
    private String reportFileName;
    private List<String> beforeGrid = new ArrayList<>();
    private ReportGrid mainGrid;
    private List<ExportOption> exportOptions = new ArrayList<>();

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public List<String> getBeforeGrid() {
        return beforeGrid;
    }

    public void setBeforeGrid(List<String> beforeGrid) {
        this.beforeGrid = beforeGrid;
    }

    public ReportGrid getMainGrid() {
        return mainGrid;
    }

    public void setMainGrid(ReportGrid mainGrid) {
        this.mainGrid = mainGrid;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public Report setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
        return this;
    }

    public List<ExportOption> getExportOptions() {
        return exportOptions;
    }

    public Report setExportOptions(List<ExportOption> exportOptions) {
        this.exportOptions = exportOptions;
        return this;
    }
}

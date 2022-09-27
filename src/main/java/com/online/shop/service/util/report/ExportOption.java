package com.online.shop.service.util.report;

public class ExportOption {

    private String link;
    private ExportType exportType;

    public String getLink() {
        return link;
    }

    public ExportOption setLink(String link) {
        this.link = link;
        return this;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public ExportOption setExportType(ExportType exportType) {
        this.exportType = exportType;
        return this;
    }
}

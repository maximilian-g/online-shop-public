package com.online.shop.service.util.report;

public enum ExportType {
    EXCEL("excel", "Export as excel");

    private final String type;
    private final String description;

    ExportType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}

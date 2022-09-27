package com.online.shop.service.util.report;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReportCell {

    private String value;
    private CellType type;

    public static ReportCell emptyStringCell() {
        return new ReportCell().setValue("").setType(CellType.STRING);
    }

    public String getValue() {
        return value;
    }

    public ReportCell setValue(String value) {
        this.value = value;
        return this;
    }

    public CellType getType() {
        return type;
    }

    @JsonIgnore
    public String getTypeStr() {
        return type.name();
    }

    public ReportCell setType(CellType type) {
        this.type = type;
        return this;
    }

}

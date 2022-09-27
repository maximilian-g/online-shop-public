package com.online.shop.service.util.report;

import java.util.List;

public class ReportGrid {

    private List<String> headers;
    private List<List<ReportCell>> rows;

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<ReportCell>> getRows() {
        return rows;
    }

    public void setRows(List<List<ReportCell>> rows) {
        this.rows = rows;
    }
}

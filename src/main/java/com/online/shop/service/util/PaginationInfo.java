package com.online.shop.service.util;

public class PaginationInfo {
    public final int pageSize;
    public final int currentPage;

    public PaginationInfo(int pageSize, int currentPage) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }
}

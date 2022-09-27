package com.online.shop.service.util;

import java.util.List;

public class Pagination <T> {
    public final CustomPage<T> page;
    public final List<Integer> pageNumbers;

    public Pagination(CustomPage<T> page, List<Integer> pageNumbers) {
        this.page = page;
        this.pageNumbers = pageNumbers;
    }
}

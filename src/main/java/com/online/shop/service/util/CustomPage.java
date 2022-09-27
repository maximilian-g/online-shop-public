package com.online.shop.service.util;

import java.util.Collection;

public class CustomPage <T> {
    public final Collection<T> content;
    // page size
    public final int size;
    // current page's number
    public final int number;
    public final int totalPages;

    public CustomPage(Collection<T> content, int size, int number, int totalPages) {
        this.content = content;
        this.size = size;
        this.number = number;
        this.totalPages = totalPages;
    }
}

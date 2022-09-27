package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class CategoryCreationException extends BusinessException {
    public CategoryCreationException(String msg) {
        super(msg);
    }

    public CategoryCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

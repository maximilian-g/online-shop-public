package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class ItemCreationException extends BusinessException {
    public ItemCreationException(String msg) {
        super(msg);
    }

    public ItemCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

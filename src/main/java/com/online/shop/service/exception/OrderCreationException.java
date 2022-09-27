package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class OrderCreationException extends BusinessException {
    public OrderCreationException(String msg) {
        super(msg);
    }

    public OrderCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

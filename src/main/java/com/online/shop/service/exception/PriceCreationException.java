package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class PriceCreationException extends BusinessException {
    public PriceCreationException(String msg) {
        super(msg);
    }

    public PriceCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

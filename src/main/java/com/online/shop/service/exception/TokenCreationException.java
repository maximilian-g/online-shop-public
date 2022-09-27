package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class TokenCreationException extends BusinessException {
    public TokenCreationException(String msg) {
        super(msg);
    }

    public TokenCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

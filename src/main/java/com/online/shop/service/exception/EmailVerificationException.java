package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class EmailVerificationException extends BusinessException {
    public EmailVerificationException(String msg) {
        super(msg);
    }

    public EmailVerificationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

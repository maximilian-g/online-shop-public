package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class PasswordRecoveryException extends BusinessException {
    public PasswordRecoveryException(String msg) {
        super(msg);
    }

    public PasswordRecoveryException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

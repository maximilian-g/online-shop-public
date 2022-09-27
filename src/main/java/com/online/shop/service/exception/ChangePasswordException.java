package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class ChangePasswordException extends BusinessException {

    public ChangePasswordException(String msg) {
        super(msg);
    }

    public ChangePasswordException(String msg, HttpStatus status) {
        super(msg, status);
    }

}

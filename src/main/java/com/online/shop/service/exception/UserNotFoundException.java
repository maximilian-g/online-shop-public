package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String msg) {
        super(msg);
    }

    public UserNotFoundException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

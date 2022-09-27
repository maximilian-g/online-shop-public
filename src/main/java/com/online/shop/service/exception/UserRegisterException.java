package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class UserRegisterException extends BusinessException {

    public UserRegisterException(String msg) {
        super(msg);
    }

    public UserRegisterException(String msg, HttpStatus status) {
        super(msg, status);
    }

}

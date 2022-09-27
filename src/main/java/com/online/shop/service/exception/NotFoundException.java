package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND);
    }

}

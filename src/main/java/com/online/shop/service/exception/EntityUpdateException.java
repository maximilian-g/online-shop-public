package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class EntityUpdateException extends BusinessException {
    public EntityUpdateException(String msg) {
        super(msg);
    }

    public EntityUpdateException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

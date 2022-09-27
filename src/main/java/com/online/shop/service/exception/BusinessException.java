package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException implements BaseException {

    private String msg;
    private final HttpStatus status;

    public BusinessException(String msg) {
        this.msg = msg;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String msg, HttpStatus status) {
        this.msg = msg;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    public void setMessage(String newMsg) {
        msg = newMsg;
    }

}

package com.online.shop.api.exception;

import com.online.shop.service.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RestException extends RuntimeException implements BaseException {

    private final String msg;
    private final HttpStatus status;

    public RestException(String msg) {
        super(msg);
        this.msg = msg;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RestException(String msg, HttpStatus httpStatus) {
        super(msg);
        this.msg = msg;
        this.status = httpStatus;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}

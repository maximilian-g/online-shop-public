package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class ExportException extends BusinessException {

    public ExportException(String msg) {
        super(msg);
    }

    public ExportException(String msg, HttpStatus status) {
        super(msg, status);
    }

}

package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class BulkUploadException extends BusinessException {

    public BulkUploadException(String msg) {
        super(msg);
    }

    public BulkUploadException(String msg, HttpStatus status) {
        super(msg, status);
    }

}

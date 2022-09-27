package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class ImageUploadException extends BusinessException {
    public ImageUploadException(String msg) {
        super(msg);
    }

    public ImageUploadException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

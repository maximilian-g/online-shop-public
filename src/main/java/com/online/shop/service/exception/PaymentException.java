package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class PaymentException extends BusinessException {

    public PaymentException(String msg) {
        super(msg);
    }

    public PaymentException(String msg, HttpStatus status) {
        super(msg, status);
    }

}

package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public class AddressCreationException extends BusinessException {

    public AddressCreationException(String msg) {
        super(msg);
    }

    public AddressCreationException(String msg, HttpStatus status) {
        super(msg, status);
    }
}

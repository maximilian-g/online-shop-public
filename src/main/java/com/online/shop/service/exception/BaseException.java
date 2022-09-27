package com.online.shop.service.exception;

import org.springframework.http.HttpStatus;

public interface BaseException {

    String getMessage();

    HttpStatus getStatus();

}

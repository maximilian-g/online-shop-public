package com.online.shop.api.util;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

public class ResponseObject {

    // indicates if something went wrong.
    // Example:
    // some entity is not found - success will be true(predictable behaviour)
    // exception occurred(during update of some entity for example) - success will be false,
    // error description will be present in responseMessage
    public final boolean success;
    public final int status;
    public final String responseMessage;
    public final Timestamp timestamp;

    public ResponseObject(boolean success, int status, String responseMessage, Timestamp timestamp) {
        this.success = success;
        this.status = status;
        this.responseMessage = responseMessage;
        this.timestamp = timestamp;
    }

    public ResponseObject(boolean success, int status, String responseMessage) {
        this.success = success;
        this.status = status;
        this.responseMessage = responseMessage;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public ResponseObject(boolean success, String responseMessage) {
        this.success = success;
        this.status = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
        this.responseMessage = responseMessage;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

}

package com.online.shop.service.util;

public class Email {

    public final String to;
    public final String subject;
    public final String body;

    public Email(String to,
                 String subject,
                 String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

}

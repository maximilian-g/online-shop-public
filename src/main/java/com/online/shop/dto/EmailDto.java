package com.online.shop.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class EmailDto {

    @NotNull(message = "Email must not be blank or empty")
    @Email(message = "Invalid email")
    private String email;

    public String getEmail() {
        return email;
    }

    public EmailDto setEmail(String email) {
        this.email = email;
        return this;
    }

}

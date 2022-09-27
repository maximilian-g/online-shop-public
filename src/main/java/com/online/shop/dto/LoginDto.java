package com.online.shop.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class LoginDto {

    @NotNull(message = "Username must not be blank.")
    @Size(min = 3, max = 45, message = "Username {app.validation.size.msg} but provided ${validatedValue}")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "Username must contain only latin symbols and numbers from 0 to 9")
    private String username;
    @NotNull(message = "Password must not be blank.")
    @Size(min = 2/*6*/, max = 45, message = "Password {app.validation.size.msg} but provided ${validatedValue}")
    private String password;

    public String getUsername() {
        return username;
    }

    public LoginDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginDto setPassword(String password) {
        this.password = password;
        return this;
    }

}

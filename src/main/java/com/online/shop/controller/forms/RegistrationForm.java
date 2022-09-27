package com.online.shop.controller.forms;

import com.online.shop.entity.Role;
import com.online.shop.entity.User;

public class RegistrationForm {

    private final String username;
    private final String email;
    private final String password;
    private final String confirm;

    public RegistrationForm(String username, String email, String password, String confirm) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirm = confirm;
    }

    public User getRawUser() {
        return new User(
                username,
                email,
                password,
                Role.USER,
                true,
                true,
                true,
                false
        );
    }

    // form considered as valid if username and password are not null
    public boolean isValid() {
        return username != null && password != null && password.equals(confirm);
    }

}

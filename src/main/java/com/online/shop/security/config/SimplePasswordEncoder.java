package com.online.shop.security.config;

import org.springframework.security.crypto.password.PasswordEncoder;

public class SimplePasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence raw, String encoded) {
        return encoded.equals(raw.toString());
    }

}

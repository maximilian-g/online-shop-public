package com.online.shop.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Value(value = "${app.encoder.strong}")
    private Boolean strongEncoder;

    @Bean
    public PasswordEncoder encoder() {
        if(!strongEncoder) {
            return new SimplePasswordEncoder();
        }
        return new BCryptPasswordEncoder();
    }

}

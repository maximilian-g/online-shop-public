package com.online.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.timezone}")
    private String appTimezone;

    public String getAppTimezone() {
        return appTimezone;
    }

}

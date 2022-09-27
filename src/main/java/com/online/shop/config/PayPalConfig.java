package com.online.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${app.url}")
    private String baseAppUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getMode() {
        return mode;
    }

    public String getBaseAppUrl() {
        return baseAppUrl;
    }
}

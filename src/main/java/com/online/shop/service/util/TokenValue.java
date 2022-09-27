package com.online.shop.service.util;

public class TokenValue {
    public final String prefix;
    public final String token;
    public final Long expirationMS;

    public TokenValue(String prefix, String token, Long expirationMS) {
        this.prefix = prefix;
        this.token = token;
        this.expirationMS = expirationMS;
    }
}

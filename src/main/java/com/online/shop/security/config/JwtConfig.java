package com.online.shop.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.claims.password}")
    private String claimsPassword;
    @Value("${app.jwt.claims.ip.key}")
    private String ipClaimKey;
    @Value("${app.jwt.exact.ip.match}")
    private Boolean exactIpClaimMatch;
    @Value("${app.jwt.roles.key}")
    private String rolesKey;
    @Value("${app.jwt.access.token.key.name}")
    private String accessTokenKeyName;
    @Value("${app.jwt.access.token.prefix}")
    private String accessTokenPrefix;
    @Value("${app.jwt.access.token.expiration}")
    private Long accessTokenExpirationMS;

    public String getSecret() {
        return secret;
    }

    public String getRolesKey() {
        return rolesKey;
    }

    public String getAccessTokenKeyName() {
        return accessTokenKeyName;
    }

    public Long getAccessTokenExpirationMS() {
        return accessTokenExpirationMS;
    }

    public String getAccessTokenPrefix() {
        return accessTokenPrefix;
    }

    public String getClaimsPassword() {
        return claimsPassword;
    }

    public String getIpClaimKey() {
        return ipClaimKey;
    }

    public Boolean isExactIpClaimMatch() {
        return exactIpClaimMatch;
    }
}

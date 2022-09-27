package com.online.shop.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.online.shop.dto.min.UserDtoMinimized;
import com.online.shop.entity.User;
import com.online.shop.security.config.JwtConfig;
import com.online.shop.service.exception.TokenCreationException;
import com.online.shop.service.util.TokenValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final UserServiceImpl userService;
    private final JwtConfig config;

    @Autowired
    protected TokenService(UserServiceImpl userService, JwtConfig config) {
        this.userService = userService;
        this.config = config;
    }

    public boolean isUserValidToCreateToken(User user) {
        return user.isEnabled() && user.isAccountNonExpired() && user.isAccountNonLocked() && user.isCredentialsNonExpired();
    }

    public Map<String, TokenValue> createTokens(String username,
                                                String requestUrl,
                                                String issuerIp) {
        User user = userService.getUserByUsername(username);
        if(!isUserValidToCreateToken(user)) {
            throw new TokenCreationException("Could not create token for '" + username + "', account is disabled or credentials expired.");
        }
        Algorithm algorithm = Algorithm.HMAC256(config.getSecret().getBytes(StandardCharsets.UTF_8));
        Long currentMillis = System.currentTimeMillis();
        JWTCreator.Builder accessTokenBuilder = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(currentMillis + config.getAccessTokenExpirationMS()))
                .withIssuer(requestUrl)
                .withClaim(config.getRolesKey(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        if(config.isExactIpClaimMatch()) {
            String encryptedIp = encryptClaim(issuerIp);
            accessTokenBuilder = accessTokenBuilder.withClaim(config.getIpClaimKey(), encryptedIp);
        }
        String accessToken = accessTokenBuilder.sign(algorithm);
        Map<String, TokenValue> tokenMap = new HashMap<>();
        tokenMap.put(config.getAccessTokenKeyName(), new TokenValue(config.getAccessTokenPrefix(), accessToken, config.getAccessTokenExpirationMS()));
        return tokenMap;
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(config.getSecret().getBytes(StandardCharsets.UTF_8));
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        UserDtoMinimized user = userService.getUserDtoByUsernameMinimized(username);
        if(!user.isEnabled()) {
            throw new RuntimeException("User is disabled. Token considered as invalid.");
        }
        userService.updateAccessDateIfNeeded(user.getId(), Instant.now());
        String[] roles = decodedJWT.getClaim(config.getRolesKey()).asArray(String.class);
        Collection<SimpleGrantedAuthority> authorityCollection = new ArrayList<>();
        if(roles != null) {
            Arrays.stream(roles).forEach(role -> {
                if(user.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase(role))) {
                    authorityCollection.add(new SimpleGrantedAuthority(role));
                }
            });
            // case when roles inside a token are not same as roles in real database
            if(authorityCollection.isEmpty()) {
                throw new RuntimeException("Data inside a token is invalid.");
            }
        }
        return new UsernamePasswordAuthenticationToken(username, null, authorityCollection);
    }

    public Cookie createTokenCookie(Map.Entry<String, TokenValue> token) {
        Cookie cookie = new Cookie(token.getKey(), token.getValue().token);
        // max age is measured in seconds
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(token.getValue().expirationMS));
        return cookie;
    }

    public Optional<String> getClaimFrom(String authString, String claimKey) {
        Algorithm algorithm = Algorithm.HMAC256(config.getSecret().getBytes(StandardCharsets.UTF_8));
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(authString);
        Optional<String> issuedToIp = Optional.ofNullable(decodedJWT.getClaim(claimKey).asString());
        if(issuedToIp.isPresent()) {
            String issuedToIpDecrypted = decryptClaim(issuedToIp.get());
            return Optional.of(issuedToIpDecrypted);
        }
        return issuedToIp;
    }

    private String decryptClaim(String claim) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = getKey(config.getClaimsPassword());
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] result = cipher.doFinal(Base64.getDecoder().decode(claim.getBytes(StandardCharsets.UTF_8)));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot process input. '" + claim + "'", ex);
        }
    }

    private String encryptClaim(String claim) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = getKey(config.getClaimsPassword());
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] result = cipher.doFinal(claim.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot process input. '" + claim + "'", ex);
        }
    }

    protected byte[] getKeyFromSecret(String secret) {
        byte[] hash = getHashInByteArray(secret.getBytes(StandardCharsets.UTF_8), "SHA-512");
        return Arrays.copyOf(hash, 16);
    }

    protected SecretKeySpec getKey(String secret) {
        return new SecretKeySpec(this.getKeyFromSecret(secret), "AES");
    }

    private static byte[] getHashInByteArray(byte[] input, String algName) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algName);

            digest.update(input);
            return digest.digest();
        } catch (Exception ex) {
            throw new RuntimeException("Could not get hash for '" + algName + "' algorithm. Error message: " + ex.getMessage(), ex);
        }
    }

}

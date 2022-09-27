package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.CartRepository;
import com.online.shop.repository.PasswordRecoveryRepository;
import com.online.shop.repository.UserRepository;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.security.config.SimplePasswordEncoder;
import com.online.shop.security.config.JwtConfig;
import com.online.shop.service.exception.TokenCreationException;
import com.online.shop.service.util.TokenValue;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    private VerificationRepository verificationRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordRecoveryRepository passwordRecoveryRepository;

    private JwtConfig config;

    private TokenService tokenService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        config = new JwtConfig();
        FieldUtils.writeField(config, "secret", "$HWreg09swanme4r5gf0pwso3n4@#G$34tf", true);
        FieldUtils.writeField(config, "rolesKey", "roles", true);
        FieldUtils.writeField(config, "accessTokenKeyName", "access_token", true);
        FieldUtils.writeField(config, "accessTokenPrefix", "Bearer ", true);
        FieldUtils.writeField(config, "accessTokenExpirationMS", 3600000L, true);
        FieldUtils.writeField(config, "exactIpClaimMatch", false, true);

        PaginationConfig paginationConfig = new PaginationConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", 8, true);

        PasswordRecoveryService passwordRecoveryService = new PasswordRecoveryService(validator, passwordRecoveryRepository);
        VerificationService verificationService = new VerificationService(validator, verificationRepository);
        AsyncEmailSenderService asyncEmailSenderService = new AsyncEmailSenderService(validator, null);
        UserServiceImpl userService = new UserServiceImpl(cartRepository,
                userRepository,
                validator,
                asyncEmailSenderService,
                verificationService,
                verificationRepository,
                passwordRecoveryService,
                paginationConfig,
                new SimplePasswordEncoder());
        tokenService = new TokenService(userService, config);

        FieldUtils.writeField(userService, "url", "http://localhost:8080", true);
        FieldUtils.writeField(userService, "verifyEmail", false, true);
        FieldUtils.writeField(userService, "accessUpdateIntervalMinutes", 30L, true);
    }

    private User getEnabledUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Bob");
        user.setIsEnabled(true);
        user.setCreationDate(LocalDateTime.now());
        user.setLastAccessDate(new Date());
        user.setIsCredentialsNonExpired(true);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setRole(Role.USER);
        return user;
    }

    private User getDisabledUser() {
        User user = getEnabledUser();
        user.setId(2L);
        user.setUsername("Rick");
        user.setIsEnabled(false);
        return user;
    }

    @Test
    void isUserValidToCreateToken() {
        User user = getEnabledUser();
        Assertions.assertTrue(tokenService.isUserValidToCreateToken(user));
        user.setIsEnabled(false);
        Assertions.assertFalse(tokenService.isUserValidToCreateToken(user));
    }

    @Test
    void createTokenPair() {
        User enabledUser = getEnabledUser();
        User disabledUser = getDisabledUser();
        BDDMockito.given(userRepository.findByUsername(enabledUser.getUsername())).willReturn(Optional.of(enabledUser));
        BDDMockito.given(userRepository.findByUsername(disabledUser.getUsername())).willReturn(Optional.of(disabledUser));
        Assertions.assertDoesNotThrow(() -> tokenService.createTokens(enabledUser.getUsername(), "http://localhost:8080", "127.0.0.1"));
        Assertions.assertThrows(TokenCreationException.class, () -> tokenService.createTokens(disabledUser.getUsername(), "http://localhost:8080", "127.0.0.1"));

        Map<String, TokenValue> tokenPair = tokenService.createTokens(enabledUser.getUsername(), "http://localhost:8080", "127.0.0.1");
        Assertions.assertEquals(1, tokenPair.size());
        Assertions.assertTrue(tokenPair.containsKey(config.getAccessTokenKeyName()));
    }

    @Test
    void getUsernamePasswordAuthenticationToken() {
        User enabledUser = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(enabledUser.getUsername())).willReturn(Optional.of(enabledUser));
        BDDMockito.given(userRepository.findById(enabledUser.getId())).willReturn(Optional.of(enabledUser));
        Map<String, TokenValue> tokenPair = tokenService.createTokens(enabledUser.getUsername(), "http://localhost:8080", "127.0.0.1");
        Assertions.assertDoesNotThrow(() -> tokenService.getUsernamePasswordAuthenticationToken(tokenPair.get(config.getAccessTokenKeyName()).token));
    }

    @Test
    void createTokenCookie() {
        User enabledUser = getEnabledUser();
        BDDMockito.given(userRepository.findByUsername(enabledUser.getUsername())).willReturn(Optional.of(enabledUser));
        Map<String, TokenValue> tokenPair = tokenService.createTokens(enabledUser.getUsername(), "http://localhost:8080", "127.0.0.1");
        for(Map.Entry<String, TokenValue> entry : tokenPair.entrySet()) {
            Cookie tokenCookie = tokenService.createTokenCookie(entry);
            Assertions.assertEquals(tokenCookie.getName(), entry.getKey());
            Assertions.assertEquals(tokenCookie.getValue(), entry.getValue().token);
            Assertions.assertEquals(tokenCookie.getMaxAge(), (int) TimeUnit.MILLISECONDS.toSeconds(entry.getValue().expirationMS));
        }
    }
}
package com.online.shop.service.impl;

import com.online.shop.entity.PasswordRecovery;
import com.online.shop.entity.User;
import com.online.shop.repository.PasswordRecoveryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.NoSuchAlgorithmException;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private PasswordRecoveryRepository passwordRecoveryRepository;

    private PasswordRecoveryService passwordRecoveryService;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        passwordRecoveryService = new PasswordRecoveryService(validator, passwordRecoveryRepository);
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Admin");
        user.setEmail("admin@gmail.com");
        return user;
    }

    @Test
    void createPasswordRecovery() throws NoSuchAlgorithmException {
        User user = getUser();
        Assertions.assertDoesNotThrow(() -> passwordRecoveryService.createPasswordRecovery(user));
        PasswordRecovery recovery = passwordRecoveryService.createPasswordRecovery(user);
        Assertions.assertFalse(recovery.getUsed());
    }

    @Test
    void exists() throws NoSuchAlgorithmException {
        User user = getUser();
        PasswordRecovery recovery = passwordRecoveryService.createPasswordRecovery(user);
        BDDMockito.given(passwordRecoveryRepository.existsById(recovery.getId())).willReturn(true);
        Assertions.assertTrue(passwordRecoveryService.exists(recovery.getId()));
    }
}
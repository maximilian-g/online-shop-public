package com.online.shop.service.impl;

import com.online.shop.entity.EmailVerification;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.util.HashingUtil;
import org.apache.commons.lang3.reflect.FieldUtils;
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
import java.util.Date;
import java.util.Random;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {
    @Mock
    private VerificationRepository verificationRepository;

    private VerificationService verificationService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        verificationService = new VerificationService(
                validator,
                verificationRepository
        );
        FieldUtils.writeField(verificationService, "expirationFromNowMS", 10000L, true);
    }

    @Test
    void exists() throws NoSuchAlgorithmException {
        EmailVerification verification = new EmailVerification();
        verification.setId(HashingUtil.getHexStrFromBytes(
                HashingUtil.getSHA256(
                        "test@gmail.com" + new Random().nextLong()
                )
        ));
        verification.setStatus(false);
        verification.setSent(false);
        verification.setTriedToSend(false);
        verification.setExpirationDate(new Date(System.currentTimeMillis() + verificationService.getExpirationFromNowMS()));
        BDDMockito.given(verificationRepository.existsById(verification.getId())).willReturn(true);
        Assertions.assertTrue(verificationService.exists(verification.getId()));
    }

    @Test
    void createVerification() throws NoSuchAlgorithmException {
        EmailVerification verification = verificationService.createVerification("test@gmail.com");
        Assertions.assertNull(verification.getHolder());
        Assertions.assertNotNull(verification.getId());
        Assertions.assertNotNull(verification.getExpirationDate());
        Assertions.assertFalse(verification.getStatus());
        Assertions.assertFalse(verification.getSent());
        Assertions.assertFalse(verification.getTriedToSend());
    }
}
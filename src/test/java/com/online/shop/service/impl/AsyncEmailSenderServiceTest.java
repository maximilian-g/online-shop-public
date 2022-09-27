package com.online.shop.service.impl;

import com.online.shop.service.util.Email;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class AsyncEmailSenderServiceTest {

    private AsyncEmailSenderService asyncEmailSenderService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        asyncEmailSenderService = new AsyncEmailSenderService(validator, emailService);
    }

    @Test
    void sendEmail() throws ExecutionException, InterruptedException {
        doNothing().when(emailService).sendEmail("test@gmail.com", "Test message", "Test body");
        Assertions.assertDoesNotThrow(() ->
                asyncEmailSenderService.sendEmail(
                        new Email("test@gmail.com", "Test message", "Test body")
                )
        );
        CompletableFuture<Boolean> result = asyncEmailSenderService.sendEmail(
                new Email("test@gmail.com", "Test message", "Test body")
        );
        Assertions.assertTrue(result.get());
    }

    @Test
    public void sendEmailMustThrow() throws ExecutionException, InterruptedException {
        doThrow(new MailSendException("Could not send email")).when(emailService).sendEmail("test@gmail.com", "Test message", "Test body");
        CompletableFuture<Boolean> result = asyncEmailSenderService.sendEmail(
                new Email("test@gmail.com", "Test message", "Test body")
        );
        Assertions.assertFalse(result.get());
    }

}
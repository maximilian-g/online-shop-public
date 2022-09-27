package com.online.shop.service.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        emailService = new EmailService(mailSender);
        FieldUtils.writeField(emailService, "fromEmailProperty", "noreply@online.shop", true);
    }

    @Test
    void sendEmail() {

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        Assertions.assertDoesNotThrow(() ->
                emailService.sendEmail(
                        "admin@gmail.com",
                        "Maintenance",
                        "Servers will be shut down in 5 minutes."
                )
        );


    }
}
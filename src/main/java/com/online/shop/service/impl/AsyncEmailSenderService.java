package com.online.shop.service.impl;

import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.util.Email;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncEmailSenderService extends BaseService {

    private final EmailService emailService;

    @Autowired
    protected AsyncEmailSenderService(Validator validator, EmailService emailService) {
        super(validator, LoggerFactory.getLogger(AsyncEmailSenderService.class));
        this.emailService = emailService;
    }

    @Async
    public CompletableFuture<Boolean> sendEmail(Email email) {
        boolean success = true;
        try {
            emailService.sendEmail(
                    email.to,
                    email.subject,
                    email.body
            );
        } catch (Exception ex) {
            logger.error("Could not email to [" + email.to + "]. Error: " + ex.getMessage());
            success = false;
        }
        return CompletableFuture.completedFuture(success);
    }

}

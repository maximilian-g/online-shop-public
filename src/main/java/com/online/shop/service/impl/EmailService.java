package com.online.shop.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.email.from.prop}")
    private String fromEmailProperty;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmailProperty);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        logger.info("Successfully send email to [" + to + "] with subject [" + subject + "]");
    }

}

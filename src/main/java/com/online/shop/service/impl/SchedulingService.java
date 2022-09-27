package com.online.shop.service.impl;

import com.online.shop.dto.OrderDto;
import com.online.shop.entity.EmailVerification;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.service.util.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class SchedulingService {

    private final Logger logger;
    private final AsyncEmailSenderService asyncEmailSenderService;
    private final VerificationRepository verificationRepository;
    private final UserServiceImpl userService;
    private final OrderServiceImpl orderService;
    @Value("${app.url}")
    private String url;
    @Value("${app.payment.days}")
    private int paymentThreshold;

    @Autowired
    protected SchedulingService(AsyncEmailSenderService asyncEmailSenderService,
                                VerificationRepository verificationRepository,
                                UserServiceImpl userService,
                                OrderServiceImpl orderService) {
        this.asyncEmailSenderService = asyncEmailSenderService;
        this.verificationRepository = verificationRepository;
        this.userService = userService;
        this.orderService = orderService;
        this.logger = LoggerFactory.getLogger(SchedulingService.class);
    }

    @Scheduled(initialDelayString = "${app.send.emails.delay.ms}", fixedRateString = "${app.send.emails.interval.ms}")
    private void tryToResendEmails() {
        Collection<EmailVerification> notSentVerifications = verificationRepository.getNotSentVerifications();
        logger.info("Emails to send: " + notSentVerifications.size());
        for (EmailVerification verification : notSentVerifications) {
            Email emailToSend = UserServiceImpl.getVerificationEmail(
                    verification.getHolder().getEmail(),
                    url,
                    verification.getId()
            );
            logger.info("Sending email to [" + emailToSend.to + "]...");
            CompletableFuture<Boolean> result = asyncEmailSenderService.sendEmail(emailToSend);
            result.thenAccept(getEmailCompleteAction(verification));
        }
    }

    @Scheduled(initialDelayString = "${app.remove.expired.delay.ms}", fixedRateString = "${app.remove.expired.interval.ms}")
    private void removeExpiredVerificationsAndDeleteUser() {
        Collection<EmailVerification> expiredVerifications = verificationRepository.getExpiredVerifications();
        logger.info("Users to delete : " + expiredVerifications.size());
        for (EmailVerification verification : expiredVerifications) {
            userService.deleteUserById(verification.getHolder().getId());
        }
    }

    @Scheduled(initialDelayString = "${app.remove.expired.delay.ms}", fixedRateString = "${app.remove.expired.interval.ms}")
    private void cancelNotPaidOrders() {
        Collection<OrderDto> orders = orderService.getNotPaidOrdersOlderThan(paymentThreshold);
        logger.info("Orders to cancel : " + orders.size());
        for (OrderDto order : orders) {
            doInTryCatch(() -> orderService.cancelOrder(order.getId(),
                    "Payment was not received within " + TimeUnit.DAYS.toHours(paymentThreshold) + " hours."),
                    this::consumeException);
        }
    }

    private Consumer<? super Boolean> getEmailCompleteAction(EmailVerification verification) {
        return (success) -> {
          if(success) {
              logger.info("Verification [" + verification.getId() + "] successfully sent.");
              verification.setSent(true);
              verificationRepository.save(verification);
          } else {
              logger.info("Could not send email verification [" + verification.getId() + "]");
          }
        };
    }

    private void doInTryCatch(Runnable runnable, Consumer<Exception> onFailure) {
        try {
            runnable.run();
        } catch (Exception ex) {
            onFailure.accept(ex);
        }
    }

    private void consumeException(Exception ex) {
        logger.warn("Exception occurred. Error: " + ex.getMessage());
    }

}

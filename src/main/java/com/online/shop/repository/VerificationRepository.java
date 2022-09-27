package com.online.shop.repository;

import com.online.shop.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface VerificationRepository extends JpaRepository<EmailVerification, String> {

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.sent <> TRUE AND ev.triedToSend = TRUE")
    Collection<EmailVerification> getNotSentVerifications();

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.status <> TRUE AND ev.expirationDate <= CURRENT_TIMESTAMP")
    Collection<EmailVerification> getExpiredVerifications();
}

package com.online.shop.service.impl;

import com.online.shop.entity.EmailVerification;
import com.online.shop.repository.VerificationRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.util.HashingUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

@Service
@Transactional
public class VerificationService extends BaseService {

    private final VerificationRepository verificationRepository;
    @Value("${app.email.expiration.ms}")
    private Long expirationFromNowMS;

    @Autowired
    protected VerificationService(Validator validator, VerificationRepository verificationRepository) {
        super(validator, LoggerFactory.getLogger(VerificationService.class));
        this.verificationRepository = verificationRepository;
    }

    public boolean exists(String verificationId) {
        return verificationRepository.existsById(verificationId);
    }

    public EmailVerification createVerification(String email) throws NoSuchAlgorithmException {
        EmailVerification result = new EmailVerification();
        result.setId(HashingUtil.getHexStrFromBytes(
                HashingUtil.getSHA256(
                        email + new Random().nextLong()
                )
        ));
        result.setStatus(false);
        result.setSent(false);
        result.setTriedToSend(false);
        result.setExpirationDate(new Date(System.currentTimeMillis() + expirationFromNowMS));
        return result;
    }

    public Long getExpirationFromNowMS() {
        return expirationFromNowMS;
    }

}

package com.online.shop.service.impl;

import com.online.shop.entity.PasswordRecovery;
import com.online.shop.entity.User;
import com.online.shop.repository.PasswordRecoveryRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.util.HashingUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Service
@Transactional
public class PasswordRecoveryService extends BaseService {

    private final PasswordRecoveryRepository passwordRecoveryRepository;

    @Autowired
    protected PasswordRecoveryService(Validator validator, PasswordRecoveryRepository passwordRecoveryRepository) {
        super(validator, LoggerFactory.getLogger(PasswordRecoveryService.class));
        this.passwordRecoveryRepository = passwordRecoveryRepository;
    }

    public PasswordRecovery createPasswordRecovery(User user) throws NoSuchAlgorithmException {
        PasswordRecovery recovery = new PasswordRecovery();
        recovery.setId(HashingUtil.getHexStrFromBytes(
                HashingUtil.getSHA256(user.getUsername() + "." + user.getEmail() + "." + new Random().nextLong())
                )
        );
        recovery.setUsed(false);
        return recovery;
    }

    public boolean exists(String recoveryId) {
        return passwordRecoveryRepository.existsById(recoveryId);
    }

    public PasswordRecovery findById(String recoveryId) {
        return passwordRecoveryRepository.findById(recoveryId).orElseThrow(() -> new NotFoundException("Could not find password recovery."));
    }

    public void delete(PasswordRecovery passwordRecovery) {
        passwordRecoveryRepository.delete(passwordRecovery);
    }

    public PasswordRecovery save(PasswordRecovery passwordRecovery) {
        return passwordRecoveryRepository.save(passwordRecovery);
    }
}

package com.online.shop.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionHandler {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T runInNewTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

}
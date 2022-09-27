package com.online.shop.service.abstraction;

import org.slf4j.Logger;

public abstract class LoggableService {

    protected final Logger logger;

    protected LoggableService(Logger logger) {
        this.logger = logger;
    }

}

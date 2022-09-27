package com.online.shop.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.shop.api.util.ResponseObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public abstract class AccessDeniedProcessor {

    protected final Logger logger;
    protected final ObjectMapper mapper;

    protected AccessDeniedProcessor(Logger logger, ObjectMapper mapper) {
        this.logger = logger;
        this.mapper = mapper;
    }

    protected void processAccessDeniedException(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Exception ex) throws ServletException, IOException {
        Optional<String> acceptHeader = Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT));
        logUserIfPresent(request);
        if(acceptHeader.isPresent() && acceptHeader.get().contains(MediaType.TEXT_HTML_VALUE)) {
            logger.debug("Sending html variant of forbidden request");
            request.getRequestDispatcher("/accessDenied")
                    .forward(request, response);
            return;
        }
        logger.debug("Sending JSON variant of forbidden request");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(),
                new ResponseObject(
                        false,
                        HttpStatus.FORBIDDEN.value(),
                        "Access to this resource is denied."
                ));
    }

    protected void logUserIfPresent(HttpServletRequest request) {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.warn("User: '" + auth.getName() +
                    "' attempted to access the protected URL: " +
                    request.getRequestURI());
        }
    }

}

package com.online.shop.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ForbiddenEntryPoint extends AccessDeniedProcessor implements AuthenticationEntryPoint {

    public ForbiddenEntryPoint() {
        super(LoggerFactory.getLogger(ForbiddenEntryPoint.class), new ObjectMapper());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        processAccessDeniedException(request, response, e);
    }

    @Override
    protected void logUserIfPresent(HttpServletRequest request) {
        logger.warn("Not authenticated user attempted to access the protected URL: " +
                request.getRequestURI());
    }
}

package com.online.shop.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CustomAccessDeniedHandler extends AccessDeniedProcessor implements AccessDeniedHandler {

    public CustomAccessDeniedHandler() {
        super(LoggerFactory.getLogger(CustomAccessDeniedHandler.class), new ObjectMapper());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        processAccessDeniedException(request, response, e);
    }

}

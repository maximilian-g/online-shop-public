package com.online.shop.security.filter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.shop.api.util.ResponseObject;
import com.online.shop.security.config.JwtConfig;
import com.online.shop.service.impl.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class JwtTokenVerifier extends OncePerRequestFilter {

    private final JwtConfig config;
    private final TokenService tokenService;
    private final ObjectMapper mapper;

    public JwtTokenVerifier(JwtConfig config, TokenService tokenService) {
        this.config = config;
        this.tokenService = tokenService;
        mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean wasExceptionThrown = false;
        if (!request.getServletPath().startsWith("/login")) {
            Optional<String> authString = Optional.ofNullable(request.getHeader(AUTHORIZATION));
            boolean hasAuthInCookie = false;
            if (authString.isEmpty()) {
                authString = getAuthCookieValFromRequest(config.getAccessTokenKeyName(), request);
                hasAuthInCookie = authString.isPresent();
            }
            if (authString.isPresent() &&
                    authString.get().startsWith(config.getAccessTokenPrefix()) || hasAuthInCookie) {
                wasExceptionThrown = !authenticate(
                        request,
                        response,
                        authString.get(),
                        hasAuthInCookie
                );
            }
        }
        if(!wasExceptionThrown) {
            filterChain.doFilter(request, response);
        }
    }

    private Optional<String> getAuthCookieValFromRequest(String authCookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(authCookieName)) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    // authenticate returns true if authentication successful, otherwise false
    public boolean authenticate(HttpServletRequest request,
                                HttpServletResponse response,
                                String authString,
                                boolean hasAuthInCookie) throws IOException {
        try {
            if (!hasAuthInCookie) {
                authString = authString.substring(config.getAccessTokenPrefix().length());
            }
            if(config.isExactIpClaimMatch()) {
                Optional<String> ipClaim = tokenService.getClaimFrom(authString, config.getIpClaimKey());
                if (ipClaim.isEmpty()) {
                    logger.warn("Could not find ip claim, token considered as not valid, logout procedure is initiated.");
                    throw new Exception("Could not find ip claim, token considered as not valid, logout procedure is initiated.");
                }
                logger.info("Comparing ip claim '" + ipClaim.get() + "' with '" + request.getRemoteAddr() + "'");
                if (!request.getRemoteAddr().equals(ipClaim.get())) {
                    logger.warn("IP claim is not same as in token, logout procedure is initiated.");
                    throw new Exception("IP claim is not same as in token, logout procedure is initiated.");
                }
            }
            SecurityContextHolder.getContext()
                    .setAuthentication(tokenService.getUsernamePasswordAuthenticationToken(authString));
            return true;
        } catch (Exception ex) {
            logger.error("Error logging in: \n" + ex.getMessage());
            String acceptHeader = Optional.ofNullable(request.getHeader(ACCEPT)).orElse("");
            if(ex instanceof JWTDecodeException) {
                sendResponse(response,
                        "Got invalid authentication credentials.",
                        acceptHeader,
                        HttpStatus.BAD_REQUEST);
            } else {
                sendResponse(response, ex.getMessage(), acceptHeader, FORBIDDEN);
            }
        }
        return false;
    }

    private void sendResponse(HttpServletResponse response,
                                    String responseMessage,
                                    String acceptHeader,
                                    HttpStatus status) throws IOException {
        if(!acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            response.setStatus(status.value());
            response.setContentType(APPLICATION_JSON_VALUE);
            ResponseObject responseObj = new ResponseObject(false, status.value(), responseMessage);
            mapper.writeValue(response.getOutputStream(), responseObj);
        } else {
            //todo requires fix
            // problem is when redirected to any view,
            // it starts authenticating using this filter and none of styles is loaded and error is not shown
            response.sendRedirect("/logout");
        }
    }

}

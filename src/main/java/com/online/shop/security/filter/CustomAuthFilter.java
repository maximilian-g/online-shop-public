package com.online.shop.security.filter;

import com.online.shop.entity.User;
import com.online.shop.service.impl.TokenService;
import com.online.shop.service.util.TokenValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class CustomAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final Logger logger;
    private final TokenService tokenService;

    public CustomAuthFilter(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.logger = LoggerFactory.getLogger(CustomAuthFilter.class);
        this.tokenService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        logger.debug("User '" + username + "' is trying to log in with password '" + password + "'");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        Map<String, TokenValue> tokenPair = tokenService.createTokens(
                user.getUsername(),
                request.getRequestURL().toString(),
                request.getRemoteAddr()
        );
        for(Map.Entry<String, TokenValue> token : tokenPair.entrySet()) {
            response.setHeader(token.getKey(), token.getValue().prefix + token.getValue().token);
            response.addCookie(tokenService.createTokenCookie(token));
        }
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException ex) throws IOException, ServletException {
        String username = request.getParameter("username");
        logger.info("User '" + username + "' could not authenticate. Type of exception: " + ex.getClass().getSimpleName());
        response.sendRedirect("/login?error");
    }

}

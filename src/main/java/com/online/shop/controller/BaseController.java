package com.online.shop.controller;

import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.exception.BusinessException;
import org.slf4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

public abstract class BaseController {

    protected final UserService userService;
    protected final Logger logger;

    protected BaseController(UserService userService, Logger logger) {
        this.userService = userService;
        this.logger = logger;
    }

    protected User getUserFromAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if(authentication == null) {
            return null;
        }
        // in this case user is not authenticated, but authentication object still will return user with username "anonymousUser"
        if(authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if(authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        } else if(authentication.getPrincipal() instanceof String) {
            String username = (String) authentication.getPrincipal();
            User user = new User();
            user.setUsername(username);
            GrantedAuthority authority = authentication.getAuthorities().iterator().next();
            user.setRole(Role.getRoleObject(authority.getAuthority()));
            return user;
        }
        return null;
    }

    protected void addUsernameAttribute(Model model, String username) {
        model.addAttribute("username", username);
    }

    protected void addAuthAttribute(Model model) {
        User user = getUserFromAuthentication();
        if(user != null) {
            userService.addAuthAttribute(model, user.getUsername());
        }
    }

    @ExceptionHandler({Exception.class})
    public ModelAndView handleException(Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("errorView");
        if(ex instanceof MethodArgumentTypeMismatchException) {
            logger.error("Error '" + ex.getMessage() + "'");
            mav.addObject("error", "Could not convert one or more parameters.");
            return mav;
        }
        if(ex instanceof BusinessException) {
            mav.addObject("error", ex.getMessage());
            return mav;
        }
        logger.error("Error '" + ex.getMessage() + "'");
        mav.addObject("error", "Unexpected error occurred...");
        mav.setViewName("error");
        return mav;
    }

}

package com.online.shop.controller;

import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/forgetPassword")
public class ForgetPasswordController extends BaseController {

    protected ForgetPasswordController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(ForgetPasswordController.class));
    }

    @GetMapping
    public String getForgetPasswordView(Model model) {
        addAuthAttribute(model);
        if (getUserFromAuthentication() != null) {
            return "home";
        }
        return "forgetPassword";
    }

    @PostMapping
    public String recoverPasswordByUsername(Model model, String username, HttpServletRequest request) {
        addAuthAttribute(model);
        if (getUserFromAuthentication() != null) {
            return "home";
        }
        userService.sendRecoveryEmail(username, request.getRemoteAddr());
        model.addAttribute(BaseService.INFO_MESSAGE_ATTRIBUTE_NAME,
                "Email with recovery data was sent to your email, " + username);
        return "forgetPassword";
    }

}

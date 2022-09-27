package com.online.shop.controller;

import com.online.shop.controller.forms.RegistrationForm;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/register")
public class RegisterController extends BaseController {

    @Autowired
    public RegisterController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(RegisterController.class));
    }

    @GetMapping
    public String getRegisterPage() {
        if (getUserFromAuthentication() != null) {
            return "redirect:/";
        } else {
            return "register";
        }
    }

    @PostMapping
    public String register(RegistrationForm userInfo, Model model, HttpServletRequest request) {
        if (getUserFromAuthentication() != null) {
            return "account";
        }
        userService.registerUser(userInfo, request.getRemoteAddr());
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME,
                "User was successfully registered, please try to login or check email and activate account.");
        return "login";
    }

}

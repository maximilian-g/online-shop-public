package com.online.shop.controller;

import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.impl.VerificationService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/verify")
public class EmailVerificationController extends BaseController {

    private final VerificationService verificationService;

    protected EmailVerificationController(UserServiceImpl userService, VerificationService verificationService) {
        super(userService, LoggerFactory.getLogger(EmailVerificationController.class));
        this.verificationService = verificationService;
    }

    @GetMapping("/{verificationId}")
    public String getVerificationView(Model model, @PathVariable String verificationId) {
        addAuthAttribute(model);
        if (verificationService.exists(verificationId)) {
            if (getUserFromAuthentication() == null) {
                if (userService.verify(verificationId)) {
                    model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Email successfully verified.");
                    return "login";
                } else {
                    model.addAttribute(ERROR_ATTRIBUTE_NAME, "Could not verify email. Please contact technical support.");
                }
            } else {
                model.addAttribute(ERROR_ATTRIBUTE_NAME, "Log out to verify email.");
            }
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Not found.");
        }
        return "error";
    }

}

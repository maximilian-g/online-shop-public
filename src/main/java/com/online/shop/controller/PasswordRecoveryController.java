package com.online.shop.controller;

import com.online.shop.controller.forms.RecoverPasswordForm;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.impl.PasswordRecoveryService;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/recoverPassword")
public class PasswordRecoveryController extends BaseController {

    private final PasswordRecoveryService passwordRecoveryService;

    protected PasswordRecoveryController(UserServiceImpl userService, PasswordRecoveryService passwordRecoveryService) {
        super(userService, LoggerFactory.getLogger(PasswordRecoveryController.class));
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @GetMapping("/{recoveryId}")
    public String getRecoveryPage(Model model, @PathVariable String recoveryId) {
        addAuthAttribute(model);
        if (getUserFromAuthentication() != null) {
            return "home";
        }
        if (passwordRecoveryService.exists(recoveryId)) {
            model.addAttribute("recoveryId", recoveryId);
            return "recoverPassword";
        }
        model.addAttribute(BaseService.ERROR_ATTRIBUTE_NAME, "Not found");
        return "error";
    }

    @PostMapping
    public String recoverPassword(Model model, RecoverPasswordForm form) {
        addAuthAttribute(model);
        if (getUserFromAuthentication() != null) {
            return "home";
        }
        userService.recoverPassword(form);
        model.addAttribute(BaseService.INFO_MESSAGE_ATTRIBUTE_NAME,
                "Password successfully recovered. Use it to log into your account.");
        return "login";
    }

}

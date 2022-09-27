package com.online.shop.controller;

import com.online.shop.controller.forms.ChangePasswordForm;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.User;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/account")
public class AccountController extends BaseController {

    @Autowired
    public AccountController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(AccountController.class));
    }

    @GetMapping
    public String getAccountPage(Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (user != null) {
            UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
            addUsernameAttribute(model, userDto.getUsername());
        }
        return "account";
    }

    @PostMapping(path = "/changePassword")
    public String changePassword(ChangePasswordForm form, Model model) {
        addAuthAttribute(model);
        User currentUser = getUserFromAuthentication();
        if (currentUser != null) {
            UserDto currentUserDto = userService.getUserDtoByUsername(currentUser.getUsername());
            addUsernameAttribute(model, currentUserDto.getUsername());
            userService.changePassword(form, currentUserDto.getId());
            model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Password successfully changed.");
        }
        return "account";
    }

}

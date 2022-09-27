package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/admin")
public class AdminMainController extends BaseController {

    protected AdminMainController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(AdminMainController.class));
    }

    @GetMapping
    public String getAdminView(Model model) {
        addAuthAttribute(model);
        return "admin";
    }

}

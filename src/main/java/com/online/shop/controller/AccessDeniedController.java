package com.online.shop.controller;

import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(path = "/accessDenied")
public class AccessDeniedController extends BaseController {
    protected AccessDeniedController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(AccessDeniedController.class));
    }

    @RequestMapping(method = {
            RequestMethod.GET,
            RequestMethod.HEAD,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.OPTIONS,
            RequestMethod.TRACE,
            RequestMethod.PATCH})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String getAccessDeniedView(Model model) {
        addAuthAttribute(model);
        return "accessDenied";
    }

}

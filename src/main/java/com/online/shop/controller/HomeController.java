package com.online.shop.controller;

import com.online.shop.dto.ItemDto;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.impl.ItemServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Controller
@RequestMapping(path = "/")
public class HomeController extends BaseController {

    private final ItemService itemService;

    public HomeController(UserServiceImpl userService, ItemServiceImpl itemService) {
        super(userService, LoggerFactory.getLogger(HomeController.class));
        this.itemService = itemService;
    }

    @GetMapping
    public String getHomePage(Model model, HttpServletRequest request) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if(user != null) {
            userService.resetLastIpIfNeeded(user.getUsername(), request.getRemoteAddr());
        }
        Collection<ItemDto> top3Items = itemService.getTopItems(3L);
        model.addAttribute("topItems", top3Items);
        return "home";
    }

}

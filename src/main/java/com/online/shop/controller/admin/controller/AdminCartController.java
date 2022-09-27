package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.service.abstraction.CartService;
import com.online.shop.service.impl.CartServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/admin/cart")
public class AdminCartController extends BaseController {

    private final CartService cartService;

    protected AdminCartController(UserServiceImpl userService, CartService cartService) {
        super(userService, LoggerFactory.getLogger(AdminCartController.class));
        this.cartService = cartService;
    }

    @GetMapping
    public String getCartsView(Model model) {
        addAuthAttribute(model);
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        return "adminCart";
    }

    @GetMapping("/{id}")
    public String getSingleCartView(Model model, @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(CartServiceImpl.SINGLE_CART_ATTRIBUTE_NAME, cartService.getById(id));
        return "adminCart";
    }

    @PostMapping("/{id}")
    public String removeAllFromCart(Model model, @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(model);
        cartService.removeAllItemsFromCart(id);
        model.addAttribute(CartServiceImpl.SINGLE_CART_ATTRIBUTE_NAME, cartService.getById(id));
        return "adminCart";
    }

}

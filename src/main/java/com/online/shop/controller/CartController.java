package com.online.shop.controller;

import com.online.shop.dto.UserDto;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.CartService;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemServiceImpl.ITEMS_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/cart")
public class CartController extends BaseController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService, UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(CartController.class));
        this.cartService = cartService;
    }

    @GetMapping
    public String getCartPage(Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
        model.addAttribute(ITEMS_ATTRIBUTE_NAME, cartService.getItemsFromCart(userDto.getCart().getId()));
        return "cart";
    }

    @PostMapping(path = "/{id}")
    public String removeItemFromCart(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
        if (cartService.removeItem(userDto.getCart().getId(), id)) {
            model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Item was successfully removed from cart.");
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Could not find item to delete.");
        }
        model.addAttribute(ITEMS_ATTRIBUTE_NAME, cartService.getItemsFromCart(userDto.getCart().getId()));
        return "cart";

    }

}

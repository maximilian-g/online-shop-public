package com.online.shop.controller;


import com.online.shop.dto.OrderDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.impl.PayPalService;
import com.online.shop.service.impl.PaymentService;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.AddressServiceImpl.ADDRESSES_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.OrderServiceImpl.MULTIPLE_ORDERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.OrderServiceImpl.SINGLE_ORDER_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/orders")
public class OrdersController extends BaseController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Autowired
    public OrdersController(OrderService orderService,
                            UserServiceImpl userService,
                            PaymentService paymentService) {
        super(userService, LoggerFactory.getLogger(OrdersController.class));
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String getOrdersPage(Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        model.addAttribute(MULTIPLE_ORDERS_ATTRIBUTE_NAME, orderService.getOrdersByUsername(user.getUsername()));
        model.addAttribute(ADDRESSES_ATTRIBUTE_NAME, userService.getAddressesOfUser(user.getUsername()));
        return "orders";
    }

    @GetMapping(path = "/{id}")
    public String getOrdersPage(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (userService.userHasOrderWithId(user.getUsername(), id)) {
            OrderDto orderDto = orderService.getById(id);
            Optional<String> paymentApprovalLink = paymentService.getPaymentApprovalLink(orderDto, user.getUsername());
            orderDto = orderService.getById(id);
            model.addAttribute(SINGLE_ORDER_ATTRIBUTE_NAME, orderDto);
            paymentApprovalLink.ifPresent(
                    link -> model.addAttribute(PayPalService.APPROVAL_LINK_ATTR, link));
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Cannot find this order for this user.");
        }
        return "order";
    }

    @GetMapping(path = "/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (userService.userHasOrderWithId(user.getUsername(), id)) {
            OrderDto orderDto = orderService.cancelOrder(id, "Cancelled by user");
            model.addAttribute(SINGLE_ORDER_ATTRIBUTE_NAME, orderDto);
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Cannot find this order for this user.");
        }
        return "order";
    }

    @GetMapping(path = "/{id}/proceed_payment")
    public String payForOrder(@PathVariable Long id,
                              @RequestParam("paymentId") String paymentId,
                              @RequestParam("PayerID") String payerId,
                              Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (userService.userHasOrderWithId(user.getUsername(), id)) {
            OrderDto orderDto = paymentService.payFor(id, paymentId, payerId);
            model.addAttribute(BaseService.INFO_MESSAGE_ATTRIBUTE_NAME,
                    "Successfully paid for order " + id);
            model.addAttribute(SINGLE_ORDER_ATTRIBUTE_NAME, orderDto);
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Cannot find this order for this user.");
        }
        return "order";
    }

    @PostMapping
    public String createOrder(@RequestParam Long addressId, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
        OrderDto order = orderService.createOrder(userDto.getCart().getId(), userDto.getId(), addressId);
        model.addAttribute(SINGLE_ORDER_ATTRIBUTE_NAME, order);
        return "redirect:/orders/" + order.getId();
    }

}

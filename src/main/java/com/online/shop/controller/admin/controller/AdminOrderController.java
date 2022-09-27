package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.OrderDto;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.impl.OrderServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.OrderServiceImpl.ORDER_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.UserServiceImpl.DEFAULT_USER_ID;

@Controller
@RequestMapping(path = "/admin/order")
public class AdminOrderController extends BaseController {

    private final OrderService orderService;

    protected AdminOrderController(UserServiceImpl userService, OrderService orderService) {
        super(userService, LoggerFactory.getLogger(AdminOrderController.class));
        this.orderService = orderService;
    }

    @GetMapping
    public String getAllOrders(@RequestParam("userId") Optional<Long> user,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model model) {
        addAuthAttribute(model);
        long userId = user.orElse(DEFAULT_USER_ID);
        addPaginatedOrdersToModel(model, userId, size.orElse(orderService.getPageSizeProp()), page.orElse(1));
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        if (userId != DEFAULT_USER_ID) {
            model.addAttribute(UserServiceImpl.SINGLE_USER_ATTRIBUTE_NAME, userService.getUserDtoById(userId));
        }
        return "adminOrders";
    }

    @GetMapping("/{id}")
    public String getSingleOrder(Model model,
                                 @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(OrderServiceImpl.SINGLE_ORDER_ATTRIBUTE_NAME, orderService.getById(id));
        return "adminOrders";
    }

    @PostMapping("/{id}")
    public String completeOrder(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        orderService.completeOrder(id);
        model.addAttribute(OrderServiceImpl.SINGLE_ORDER_ATTRIBUTE_NAME, orderService.getById(id));
        return "adminOrders";
    }

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        orderService.deleteOrderById(id);
        addPaginatedOrdersToModel(model, DEFAULT_USER_ID, orderService.getPageSizeProp(), 1);
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        return "adminOrders";
    }

    private void addPaginatedOrdersToModel(Model model, Long userId, int pageSize, int currentPage) {
        Pagination<OrderDto> pagination = orderService.findPaginated(userId,
                pageSize,
                currentPage);
        model.addAttribute(ORDER_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
    }

}

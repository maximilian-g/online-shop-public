package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.OrderDto;
import com.online.shop.entity.OrderStatus;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.impl.OrderServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.OrderServiceImpl.ORDER_PAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/operator")
public class OperatorController  extends BaseController {

    private final OrderService orderService;

    protected OperatorController(UserService userService, OrderServiceImpl orderService) {
        super(userService, LoggerFactory.getLogger(OperatorController.class));
        this.orderService = orderService;
    }

    @GetMapping
    public String getOperatorView(@RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size,
                                  @RequestParam(name = "status", defaultValue = "") String status,
                                  Model model) {
        addAuthAttribute(model);
        OrderStatus orderStatus = OrderStatus.getStatusObject(status);
        if(orderStatus != null) {
            addPaginatedOrdersToModel(model,
                    orderService.findPaginatedNotCompletedWithStatusOrderByIdDesc(
                            orderStatus,
                            size.orElse(orderService.getPageSizeProp()),
                            page.orElse(1)
                    ));
        } else {
            addPaginatedOrdersToModel(model,
                    orderService.findPaginatedNotCompletedOrderByIdDesc(
                            size.orElse(orderService.getPageSizeProp()),
                            page.orElse(1)
                    ));
        }
        if(orderStatus != null) {
            model.addAttribute("status", orderStatus.getStatus());
        }

        return "operator";
    }

    @GetMapping("/order/{id}")
    public String getOperatorView(@PathVariable Long id,
                                  Model model) {
        addAuthAttribute(model);
        OrderDto order = orderService.getById(id);
        // operator cannot list completed orders
        if(!order.getFormattedEndDate().isEmpty()) {
            throw new NotFoundException("Order with id '" + id + "' not found");
        }
        model.addAttribute(OrderServiceImpl.SINGLE_ORDER_ATTRIBUTE_NAME, order);
        return "operator";
    }

    private void addPaginatedOrdersToModel(Model model, Pagination<OrderDto> pagination) {
        model.addAttribute(ORDER_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
    }

}

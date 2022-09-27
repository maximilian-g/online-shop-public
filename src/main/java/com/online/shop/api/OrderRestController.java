package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.OrderDto;
import com.online.shop.dto.OrderStatusDto;
import com.online.shop.dto.UserDto;
import com.online.shop.dto.transfer.IdOnly;
import com.online.shop.entity.OrderStatus;
import com.online.shop.entity.Role;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.exception.EntityUpdateException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderRestController extends BaseRestController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderRestController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
        if (user.getOrderIds().stream().anyMatch(o -> o.equals(id)) || isAdmin(user.getAuthorities())) {
            return ResponseEntity.ok(orderService.getById(id));
        }
        throw getPermissionRestException();
    }

    @GetMapping(value = "/statuses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getOrderStatuses() {
        return ResponseEntity.ok(OrderStatus.getStatuses());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> createOrder(@Validated({IdOnly.class}) @RequestBody AddressDto address) {
        UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
        return ResponseEntity.ok(orderService.createOrder(user.getCart().getId(), user.getId(), address.getId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> updateStatus(@PathVariable Long id,
                                                 @RequestBody @Valid OrderStatusDto orderStatus) {
        UserDto userDtoByUsername = userService.getUserDtoByUsername(getCurrentUserUsername());
        OrderDto orderDto = orderService.getById(id);
        OrderStatus status = OrderStatus.getStatusObject(orderDto.getStatus());
        if (Role.OPERATOR.getRole().equals(userDtoByUsername.getRole()) &&
                OrderStatus.PAID.getSequenceNumber() > status.getSequenceNumber()) {
            throw new EntityUpdateException("Order is waiting for payment, cannot change status.");
        }
        return ResponseEntity.ok(orderService.updateStatus(id, orderStatus));
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok(new ResponseObject(true, "Order successfully deleted."));
    }

}

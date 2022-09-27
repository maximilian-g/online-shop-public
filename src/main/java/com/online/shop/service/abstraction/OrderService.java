package com.online.shop.service.abstraction;

import com.online.shop.dto.OrderDto;
import com.online.shop.dto.OrderStatusDto;
import com.online.shop.entity.OrderStatus;
import com.online.shop.service.util.Pagination;

import java.util.Collection;
import java.util.Date;

public interface OrderService {

    OrderDto createOrder(Long cartId, Long userId, Long addressId);

    Collection<OrderDto> getOrdersByUsername(String username);

    Pagination<OrderDto> findPaginated(long userID, int pageSize, int currentPage);

    Pagination<OrderDto> findPaginatedNotCompletedOrderByIdDesc(int pageSize, int currentPage);

    Pagination<OrderDto> findPaginatedNotCompletedWithStatusOrderByIdDesc(OrderStatus status,  int pageSize, int currentPage);

    int getPageSizeProp();

    OrderDto getById(Long orderId);

    OrderDto completeOrder(Long orderId);

    OrderDto cancelOrder(Long orderId, String description);

    OrderDto updateStatus(Long orderId, OrderStatusDto orderStatus);

    void deleteOrderById(Long orderId);

    void addPaymentId(Long orderId, String paymentId);

    Collection<OrderDto> getNotPaidOrdersOlderThan(int days);

    Collection<OrderDto> getPaidOrdersForPeriod(Date startDate, Date endDate);

}

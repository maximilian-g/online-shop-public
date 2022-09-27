package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.OrderDto;
import com.online.shop.dto.OrderStatusDto;
import com.online.shop.entity.Address;
import com.online.shop.entity.Cart;
import com.online.shop.entity.CartItem;
import com.online.shop.entity.Item;
import com.online.shop.entity.Order;
import com.online.shop.entity.OrderItem;
import com.online.shop.entity.OrderStatus;
import com.online.shop.entity.User;
import com.online.shop.repository.OrderItemRepository;
import com.online.shop.repository.OrderRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.OrderService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.exception.OrderCreationException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl extends BaseService implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final CartServiceImpl cartService;
    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;

    private final PaginationConfig paginationConfig;

    public static final String MULTIPLE_ORDERS_ATTRIBUTE_NAME = "orders";
    public static final String SINGLE_ORDER_ATTRIBUTE_NAME = "order";
    public static final String ORDER_PAGE_ATTRIBUTE_NAME = "orderPage";


    @Autowired
    protected OrderServiceImpl(OrderRepository orderRepository,
                               CartServiceImpl cartService,
                               Validator validator,
                               OrderItemRepository orderItemRepository,
                               UserServiceImpl userService,
                               ItemServiceImpl itemService,
                               PaginationConfig paginationConfig) {
        super(validator, LoggerFactory.getLogger(OrderServiceImpl.class));
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.paginationConfig = paginationConfig;
    }

    @Override
    public OrderDto createOrder(Long cartId, Long userId, Long addressId) {
        return OrderDto.getOrderDto(createOrderEntity(cartId, userId, addressId));
    }

    @Override
    public OrderDto completeOrder(Long orderId) {
        Order order = getOrderByIdForUpdate(orderId);
        return completeOrder(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public OrderDto cancelOrder(Long orderId, String description) {
        Order order = getOrderByIdForUpdate(orderId);
        return cancelOrder(order, description);
    }

    @Override
    public OrderDto updateStatus(Long orderId, OrderStatusDto orderStatus) {
        Order order = getOrderByIdForUpdate(orderId);
        if(!OrderStatus.isValidStatus(orderStatus.getStatus())) {
            throw new EntityUpdateException("Order status '" + orderStatus.getStatus() + "' is not valid.");
        }
        OrderStatus newStatus = OrderStatus.getStatusObject(orderStatus.getStatus());
        if(newStatus == order.getStatus()) {
            throw new EntityUpdateException("Order already has status '" + orderStatus.getStatus() + "'.");
        }
        // Order cannot be updated with status if new order status sequence number is less than current
        // includes order.getStatus() == OrderStatus.COMPLETED case
        // includes order.getStatus() == OrderStatus.PAID case
        if(newStatus.getSequenceNumber() < order.getStatus().getSequenceNumber()) {
            throw new EntityUpdateException("Cannot set status '" + newStatus.getStatus() +
                    "', order already completed this stage.");
        }
        if(newStatus == OrderStatus.COMPLETED) {
            if(!order.isPaid()) {
                throw new EntityUpdateException("Cannot complete order without payment.");
            }
            return completeOrder(order);
        }
        if(newStatus == OrderStatus.CANCELLED) {
            if(orderStatus.getDescription() == null || orderStatus.getDescription().isEmpty()) {
                throw new EntityUpdateException("Reason of cancellation must be provided.");
            }
            return cancelOrder(order, orderStatus.getDescription());
        }
        if(newStatus == OrderStatus.PAID) {
            order.setPaid(true);
        }
        order.setStatus(newStatus);
        order.setDescription(newStatus.getDescription());
        return OrderDto.getOrderDto(orderRepository.save(order));
    }

    @Override
    public void deleteOrderById(Long orderId) {
        Order order = getOrderByIdForUpdate(orderId);
        if (order.isCompleted()) {
            throw new EntityUpdateException("Cannot delete completed order.");
        }
        removeItemsFromOrder(order);
        order.getOrderItems().clear();
        order = orderRepository.save(order);
        orderRepository.delete(order);
    }

    @Override
    public void addPaymentId(Long orderId, String paymentId) {
        Order order = getOrderByIdForUpdate(orderId);
        if(order.getPaymentId() != null) {
            throw new EntityUpdateException("Order " + orderId + " already has pending payment.");
        }
        order.setPaymentId(paymentId);
    }

    @Override
    public Collection<OrderDto> getNotPaidOrdersOlderThan(int days) {
        Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days));
        return OrderDto.getOrderDtoCollection(orderRepository.getNotPaidOrdersOlderThanAndNotInStatuses(
                date,
                List.of(OrderStatus.CANCELLED,
                        OrderStatus.PAID,
                        OrderStatus.SENT,
                        OrderStatus.DELIVERED,
                        OrderStatus.COMPLETED)
        ));
    }

    @Override
    public Collection<OrderDto> getPaidOrdersForPeriod(Date startDate, Date endDate) {
        if(startDate.after(endDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date.");
        }
        return OrderDto.getOrderDtoCollection(orderRepository.getPaidOrdersForPeriodOrderByIdDesc(startDate, endDate));
    }

    @Override
    public Pagination<OrderDto> findPaginated(long userID, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(orderRepository.count(), pageSize, currentPage, paginationConfig.getPageSizeProp());
        Page<Order> orderPage;
        if (UserServiceImpl.DEFAULT_USER_ID.equals(userID)) {
            orderPage = orderRepository.findAll(
                    PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
            );
        } else {
            orderPage = orderRepository.getOrdersByUserId(userID,
                    PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
            );
        }
        return new Pagination<>(
                new CustomPage<>(
                        orderPage.getContent().stream().map(OrderDto::getOrderDto).collect(Collectors.toList()),
                        orderPage.getSize(),
                        orderPage.getNumber(),
                        orderPage.getTotalPages()
                ),
                getPageNumbers(orderPage.getTotalPages(),
                        paginationInfo.currentPage));
    }

    @Override
    public Pagination<OrderDto> findPaginatedNotCompletedOrderByIdDesc(int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(orderRepository.getAllNotCompletedCount(),
                        pageSize,
                        currentPage,
                        paginationConfig.getPageSizeProp());
        Page<Order> orderPage = orderRepository.getAllNotCompletedOrderByIdDesc(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        orderPage.getContent().stream().map(OrderDto::getOrderDto).collect(Collectors.toList()),
                        orderPage.getSize(),
                        orderPage.getNumber(),
                        orderPage.getTotalPages()
                ),
                getPageNumbers(orderPage.getTotalPages(),
                        paginationInfo.currentPage));
    }

    @Override
    public Pagination<OrderDto> findPaginatedNotCompletedWithStatusOrderByIdDesc(OrderStatus status,
                                                                                 int pageSize,
                                                                                 int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(orderRepository.getAllNotCompletedWithStatusCount(status),
                        pageSize,
                        currentPage,
                        paginationConfig.getPageSizeProp());
        Page<Order> orderPage = orderRepository.getAllNotCompletedWithStatusOrderByIdDesc(status,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        orderPage.getContent().stream().map(OrderDto::getOrderDto).collect(Collectors.toList()),
                        orderPage.getSize(),
                        orderPage.getNumber(),
                        orderPage.getTotalPages()
                ),
                getPageNumbers(orderPage.getTotalPages(),
                        paginationInfo.currentPage));
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    @Override
    public OrderDto getById(Long orderId) {
        return OrderDto.getOrderDto(getOrderById(orderId));
    }

    @Override
    public Collection<OrderDto> getOrdersByUsername(String username) {
        return getOrders(username).stream().map(OrderDto::getOrderDto).collect(Collectors.toList());
    }

    public OrderDto completeOrder(Order order) {
        order.setStatus(OrderStatus.COMPLETED);
        order.setDescription(order.getStatus().getDescription());
        order.setEndDate(new Date(System.currentTimeMillis()));
        return OrderDto.getOrderDto(orderRepository.save(order));
    }

    public OrderDto cancelOrder(Order order, String description) {
        if(order.getStatus().getSequenceNumber() >= OrderStatus.CANCELLED.getSequenceNumber()) {
            throw new EntityUpdateException("Cannot cancel order #" + order.getId() + ".");
        }
        logger.info("Cancelling order " + order.getId());
        order.setStatus(OrderStatus.CANCELLED);
        order.setDescription(description);
        // add item quantity that were removed when order was created
        for(OrderItem item : order.getOrderItems()) {
            itemService.addItemQuantity(item.getId().getItemId(), item.getQuantity(), order.getId());
        }
        order.setEndDate(new Date(System.currentTimeMillis()));
        order = orderRepository.saveAndFlush(order);
        return OrderDto.getOrderDto(order);
    }

    public Order createOrderEntity(Long cartId, Long userId, Long addressId) {
        Order order;
        Cart cart = cartService.getCartByIdForUpdate(cartId);
        User user = userService.getUserById(userId);
        if (user.getId().equals(cart.getUser().getId())) {
            if (cart.getCartItems().size() != 0) {
                if (user.getAddresses().stream().anyMatch(userAddress -> userAddress.getId().equals(addressId))) {
                    List<CartItem> items = new ArrayList<>(cart.getCartItems());
                    List<OrderItem> orderItems = new ArrayList<>(items.size());
                    order = new Order();
                    order.setUser(user);
                    order.setStartDate(new Date(System.currentTimeMillis()));
                    order.setStatus(OrderStatus.CREATED);
                    order.setDescription(order.getStatus().getDescription());
                    order.setPaid(false);
                    Address address = new Address();
                    address.setId(addressId);
                    order.setAddress(address);
                    order = orderRepository.save(order);
                    for (CartItem cartItem : items) {
                        if (itemService.getItemQuantityInStock(cartItem.getItem().getId()) < cartItem.getQuantity()) {
                            throw new OrderCreationException("Item '" + cartItem.getItem().getName() + "' is not in stock for now.");
                        }
                        Item item = cartItem.getItem();
                        itemService.addItemQuantity(item.getId(), -cartItem.getQuantity(), order.getId());
                    }
                    for (CartItem cartItem : items) {
                        orderItems.add(OrderItem.OrderItemBuilder
                                .createBuilder()
                                .id(order.getId(), cartItem.getItem().getId())
                                .quantity(cartItem.getQuantity())
                                .item(cartItem.getItem())
                                .order(order)
                                .build());
                    }
                    order.setOrderItems(orderItems);
                    order.getOrderItems().forEach(orderItemRepository::save);
                } else {
                    throw new OrderCreationException("Cannot create order for this address.");
                }
            } else {
                throw new OrderCreationException("Cannot create order. Cart is empty.");
            }
        } else {
            throw new OrderCreationException("Invalid data. Could not match user with cart.");
        }
        cartService.removeAllItemsFromCart(cart);
        return order;
    }

    public Collection<Order> getOrders(String username) {
        return orderRepository.getOrdersByUsernameOrderByIdDesc(username);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id '" + orderId + "' not found."));
    }

    public Order getOrderByIdForUpdate(Long orderId) {
        return orderRepository.getOrderByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id '" + orderId + "' not found."));
    }

    protected void removeItemsFromOrder(Order order) {
        order.getOrderItems().forEach(orderItemRepository::delete);
    }
}

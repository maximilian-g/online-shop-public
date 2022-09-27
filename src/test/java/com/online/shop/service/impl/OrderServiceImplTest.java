package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.dto.OrderDto;
import com.online.shop.dto.OrderStatusDto;
import com.online.shop.entity.Address;
import com.online.shop.entity.Cart;
import com.online.shop.entity.CartItem;
import com.online.shop.entity.Category;
import com.online.shop.entity.Item;
import com.online.shop.entity.ItemQuantityChange;
import com.online.shop.entity.ItemType;
import com.online.shop.entity.Order;
import com.online.shop.entity.OrderItem;
import com.online.shop.entity.OrderStatus;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.OrderItemRepository;
import com.online.shop.repository.OrderRepository;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.exception.OrderCreationException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final int PAGE_SIZE_PROP = 8;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartServiceImpl cartService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ItemServiceImpl itemService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        PaginationConfig paginationConfig = new PaginationConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", PAGE_SIZE_PROP, true);
        orderService = new OrderServiceImpl(
                orderRepository,
                cartService,
                validator,
                orderItemRepository,
                userService,
                itemService, paginationConfig
        );
    }

    private Cart getEmptyCart() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setCartItems(new ArrayList<>());
        return cart;
    }

    private Cart getCartWithItems(long itemsToOrder) {
        Cart cart = getEmptyCart();
        Item item = new Item();
        item.setId(1L);
        item.setName("Keyboard");

        ItemQuantityChange itemQuantityChange = new ItemQuantityChange();
        itemQuantityChange.setId(1L);
        itemQuantityChange.setItem(item);
        itemQuantityChange.setChangeDate(new Date());
        itemQuantityChange.setChange(itemsToOrder + 50L);
        item.getChanges().add(itemQuantityChange);

        item.setOrders(new ArrayList<>());
        item.setPrices(new ArrayList<>());
        item.setDescription("Razer keyboard");

        Category category = new Category();
        category.setId(1L);
        category.setName("Computer devices");
        category.setAssociatedItemsCount(item.getQuantity());

        ItemType type = new ItemType();
        type.setId(1L);
        type.setName("Keyboards");
        type.setCategory(category);
        category.setItemTypes(List.of(type));

        item.setType(type);

        CartItem cartItem = CartItem.CartItemBuilder
                .createBuilder()
                .id(cart.getId(), item.getId())
                .item(item)
                .cart(cart)
                .quantity(itemsToOrder)
                .build();

        cart.getCartItems().add(cartItem);
        return cart;
    }

    private User getUser(Cart cart) {
        User user = new User();
        user.setId(1L);

        user.setUsername("Customer");
        user.setPassword("495nghs4J^RETh");

        cart.setUser(user);
        user.setCart(cart);

        Address address = getAddress();
        address.setOrders(new ArrayList<>());
        address.setUser(user);
        user.setAddresses(List.of(address));

        user.setRole(Role.USER);
        user.setOrders(new ArrayList<>());
        user.setIsEnabled(true);
        user.setEmail("customer@gmail.com");
        return user;
    }

    private Address getAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setAddress("Rotterdam");
        return address;
    }

    private Order getOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStartDate(new java.sql.Date(System.currentTimeMillis()));

        User user = new User();
        user.setUsername("admin");
        user.setId(1L);

        order.setUser(user);
        user.setOrders(List.of(order));

        order.setAddress(getAddress());

        order.setOrderItems(Collections.emptyList());
        order.setStatus(OrderStatus.CREATED);
        order.setPaid(false);
        return order;
    }


    @Test
    void createOrder() {
        final long itemsToOrder = 5L;
        User user = getUser(getCartWithItems(itemsToOrder));

        BDDMockito.given(cartService.getCartByIdForUpdate(user.getCart().getId())).willReturn(user.getCart());
        BDDMockito.given(userService.getUserById(user.getId())).willReturn(user);
        when(orderRepository.save(any(Order.class))).then((Answer<Order>) invocationOnMock -> {
            Order order = invocationOnMock.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderItemRepository.save(any(OrderItem.class))).then(AdditionalAnswers.returnsFirstArg());
        doNothing().when(cartService).removeAllItemsFromCart(any(Cart.class));

        Address address = user.getAddresses().iterator().next();
        CartItem cartItem = user.getCart().getCartItems().iterator().next();
        final long itemQuantityBefore = cartItem.getItem().getQuantity();

        when(itemService.getItemQuantityInStock(any(Long.class)))
                .then((Answer<Long>) invocationOnMock -> cartItem.getItem().getQuantity());

        when(itemService.addItemQuantity(any(Long.class), any(Long.class), any(Long.class))).then((Answer<ItemDto>) invocationOnMock -> {
            Long quantityToAdd = invocationOnMock.getArgument(1);

            Item item = cartItem.getItem();

            ItemQuantityChange itemQuantityChange = new ItemQuantityChange();
            itemQuantityChange.setId(2L);
            itemQuantityChange.setItem(item);
            itemQuantityChange.setChangeDate(new Date());
            itemQuantityChange.setChange(quantityToAdd);
            item.getChanges().add(itemQuantityChange);

            return ItemDto.getItemDto(item);
        });

        OrderDto order = orderService.createOrder(1L, 1L, 1L);
        Assertions.assertSame(1L, order.getId());
        Assertions.assertTrue(System.currentTimeMillis() - order.getStartDate().getTime() < 1000L);
        Assertions.assertNull(order.getEndDate());
        Assertions.assertFalse(order.isPaid());
        Assertions.assertFalse(order.isCompleted());
        Assertions.assertNotNull(order.getAddress());

        Assertions.assertEquals(address.getId(), order.getAddress().getId());

        Assertions.assertEquals(1, order.getOrderItems().size());

        ItemQuantityDto dto = order.getOrderItems().iterator().next();

        Assertions.assertEquals(cartItem.getItem().getId(), dto.getItem().getId());
        Assertions.assertEquals(cartItem.getItem().getName(), dto.getItem().getName());
        Assertions.assertEquals(cartItem.getItem().getDescription(), dto.getItem().getDescription());
        Assertions.assertEquals(cartItem.getQuantity(), dto.getQuantity());

        Assertions.assertEquals(itemQuantityBefore - cartItem.getQuantity(), cartItem.getItem().getQuantity());

        cartItem.setQuantity(cartItem.getItem().getQuantity() + 5);
        Assertions.assertThrows(OrderCreationException.class, () -> orderService.createOrder(1L, 1L, 1L));

        cartItem.setQuantity(1L);
        User invalidUser = new User();
        invalidUser.setId(2L);
        user.getCart().setUser(invalidUser);
        Assertions.assertThrows(OrderCreationException.class, () -> orderService.createOrder(1L, 1L, 1L));


        user.getCart().setUser(user);
        user.getCart().setCartItems(Collections.emptyList());
        Assertions.assertThrows(OrderCreationException.class, () -> orderService.createOrder(1L, 1L, 1L));

        user.setAddresses(Collections.emptyList());
        user.getCart().setCartItems(List.of(cartItem));
        Assertions.assertThrows(OrderCreationException.class, () -> orderService.createOrder(1L, 1L, 1L));



    }

    @Test
    void completeOrder() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrderByIdForUpdate(order.getId())).willReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());

        orderService.completeOrder(order.getId());
        Assertions.assertTrue(OrderStatus.COMPLETED.getDescription().equalsIgnoreCase(order.getDescription()));
        Assertions.assertTrue(order.isCompleted());
        Assertions.assertNotNull(order.getEndDate());
        Assertions.assertTrue(System.currentTimeMillis() - order.getEndDate().getTime() < 1000L);
    }

    @Test
    void deleteOrderById() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrderByIdForUpdate(order.getId())).willReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());
        doNothing().when(orderRepository).delete(any(Order.class));

        Assertions.assertDoesNotThrow(() -> orderService.deleteOrderById(order.getId()));

        order.setStatus(OrderStatus.COMPLETED);
        Assertions.assertThrows(EntityUpdateException.class, () -> orderService.deleteOrderById(order.getId()));
    }

    @Test
    void getPageSizeProp() {
        Assertions.assertEquals(PAGE_SIZE_PROP, orderService.getPageSizeProp());
    }

    @Test
    void getById() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        BDDMockito.given(orderRepository.findById(2L)).willReturn(Optional.empty());

        Assertions.assertDoesNotThrow(() -> orderService.getById(order.getId()));
        Assertions.assertThrows(NotFoundException.class, () -> orderService.getById(2L));
    }

    @Test
    void getOrdersByUsername() {

        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrdersByUsernameOrderByIdDesc(order.getUser().getUsername())).willReturn(List.of(order));

        Collection<OrderDto> ordersByUsername = orderService.getOrdersByUsername(order.getUser().getUsername());
        Assertions.assertEquals(1, ordersByUsername.size());

    }

    @Test
    void cancelOrder() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrderByIdForUpdate(order.getId())).willReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());

        OrderDto orderRes = orderService.cancelOrder(order.getId(), "Cancelled by user");
        Assertions.assertEquals("Cancelled by user", orderRes.getDescription());
        Assertions.assertEquals(OrderStatus.CANCELLED.name(), orderRes.getStatus());
        Assertions.assertNotNull(orderRes.getEndDate());
    }

    @Test
    void updateStatus() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrderByIdForUpdate(order.getId())).willReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());
        when(orderRepository.save(any(Order.class))).then(AdditionalAnswers.returnsFirstArg());

        OrderStatusDto dto = new OrderStatusDto();
        dto.setDescription("Some desc");
        dto.setStatus("Invalid status");

        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

        dto.setStatus(OrderStatus.CANCELLED.getStatus());
        dto.setDescription(null);
        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

        dto.setDescription("Some desc");
        Assertions.assertDoesNotThrow(
                () -> orderService.updateStatus(order.getId(), dto));
        Assertions.assertEquals(OrderStatus.CANCELLED, order.getStatus());
        Assertions.assertNotNull(order.getEndDate());
        order.setStatus(OrderStatus.CREATED);
        order.setEndDate(null);

        dto.setStatus(OrderStatus.COMPLETED.getStatus());
        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

        dto.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getStatus());
        orderService.updateStatus(order.getId(), dto);
        dto.setStatus(OrderStatus.CREATED.getStatus());

        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

        order.setStatus(OrderStatus.COMPLETED);
        dto.setStatus(OrderStatus.CANCELLED.getStatus());
        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

        dto.setDescription(null);
        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));
        dto.setDescription("Some desc");

        dto.setStatus(OrderStatus.PAID.getStatus());
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT);
        orderService.updateStatus(order.getId(), dto);
        Assertions.assertEquals(OrderStatus.PAID, order.getStatus());
        Assertions.assertTrue(order.isPaid());

        dto.setStatus(OrderStatus.CANCELLED.getStatus());
        Assertions.assertThrows(EntityUpdateException.class,
                () -> orderService.updateStatus(order.getId(), dto));

    }

    @Test
    void addPaymentId() {
        Order order = getOrder();
        BDDMockito.given(orderRepository.getOrderByIdForUpdate(order.getId())).willReturn(Optional.of(order));
        String paymentId = "Some payment id";
        Assertions.assertDoesNotThrow(() -> orderService.addPaymentId(order.getId(), paymentId));
        Assertions.assertEquals(paymentId, order.getPaymentId());
        Assertions.assertThrows(EntityUpdateException.class, () -> orderService.addPaymentId(order.getId(), paymentId));
        Assertions.assertEquals(paymentId, order.getPaymentId());
    }

    // add payment id
}
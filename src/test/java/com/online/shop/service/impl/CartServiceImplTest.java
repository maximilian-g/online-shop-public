package com.online.shop.service.impl;

import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.entity.Cart;
import com.online.shop.entity.CartItem;
import com.online.shop.entity.Category;
import com.online.shop.entity.Item;
import com.online.shop.entity.ItemType;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.repository.CartItemRepository;
import com.online.shop.repository.CartRepository;
import com.online.shop.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        cartService = new CartServiceImpl(cartRepository, validator, cartItemRepository);
    }

    private Cart getCart() {
        Cart cart = new Cart();
        cart.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        user.setCart(cart);
        cart.setUser(user);

        Item firstItem = new Item();
        firstItem.setId(1L);
//        firstItem.setQuantity(5L);
        firstItem.setName("Motherboard");

        Category firstCategory = new Category();
        firstCategory.setId(1L);
        firstCategory.setName("Computer devices or parts");
        firstCategory.setAssociatedItemsCount(1L);
        ItemType type = new ItemType();
        type.setId(1L);
        type.setName("Motherboards");
        type.setCategory(firstCategory);
        firstCategory.setItemTypes(List.of(type));
        firstItem.setType(type);

        Item secondItem = new Item();
        secondItem.setId(2L);
//        secondItem.setQuantity(5L);
        secondItem.setName("Car tire");
        Category secondCategory = new Category();
        secondCategory.setId(1L);
        secondCategory.setName("Computer devices or parts");
        secondCategory.setAssociatedItemsCount(1L);
        type = new ItemType();
        type.setId(2L);
        type.setName("Other");
        type.setCategory(firstCategory);
        secondCategory.setItemTypes(List.of(type));
        secondItem.setType(type);

        CartItem firstCartItem = CartItem.CartItemBuilder
                .createBuilder()
                .id(1L, 1L)
                .cart(cart)
                .item(firstItem)
                .quantity(1L)
                .build();

        CartItem secondCartItem = CartItem.CartItemBuilder
                .createBuilder()
                .id(1L, 2L)
                .cart(cart)
                .item(secondItem)
                .quantity(1L)
                .build();
        List<CartItem> cartItemList = new ArrayList<>();
        cartItemList.add(firstCartItem);
        cartItemList.add(secondCartItem);

        cart.setCartItems(cartItemList);
        return cart;
    }

    private Item getNewItem() {
        Item item = new Item();
        item.setId(5L);
        item.setName("Mousepad");
        item.setDescription("Cloth mousepad");

        Category category = new Category();
        category.setId(4L);
        category.setName("Computer devices or parts");
        category.setAssociatedItemsCount(1L);
        ItemType type = new ItemType();
        type.setId(1L);
        type.setName("Mouse items");
        type.setCategory(category);
        category.setItemTypes(List.of(type));
        item.setType(type);

//        item.setQuantity(100L);
        item.setPrices(Collections.emptyList());
        item.setOrders(Collections.emptyList());
        return item;
    }

    @Test
    void getItemsFromCart() {
        Cart cart = getCart();
        BDDMockito.given(cartRepository.findById(cart.getId())).willReturn(Optional.of(cart));
        BDDMockito.given(cartRepository.findById(2L)).willReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> cartService.getItemsFromCart(cart.getId()));
        Collection<ItemQuantityDto> items = cartService.getItemsFromCart(cart.getId());
        Assertions.assertEquals(2, items.size());
        Assertions.assertThrows(NotFoundException.class, () -> cartService.getItemsFromCart(2L));
    }

    @Test
    void updateItemInCart() {
        Cart cart = getCart();
        BDDMockito.given(cartRepository.getCartByIdForUpdate(cart.getId())).willReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).then(AdditionalAnswers.returnsFirstArg());
        Assertions.assertDoesNotThrow(() -> cartService.updateItemInCart(1L, 1L, 3));

        Optional<CartItem> cartItemOpt = cart.getCartItems().stream()
                .filter(ca -> ca.getItem().getId().equals(1L))
                .findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();
        Assertions.assertEquals(4L, cartItem.getQuantity());
    }

    @Test
    void removeItem() {
        Cart cart = getCart();
        BDDMockito.given(cartRepository.getCartByIdForUpdate(cart.getId())).willReturn(Optional.of(cart));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        Assertions.assertDoesNotThrow(() -> cartService.removeItem(1L, 1L));


        CartItem firstItem = cart.getCartItems().stream()
                .filter(ca -> ca.getItem().getId().equals(1L))
                .collect(Collectors.toList())
                .get(0);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        // capturing an item
        Mockito.verify(cartItemRepository).delete(cartItemArgumentCaptor.capture());
        CartItem captured = cartItemArgumentCaptor.getValue();
        Assertions.assertSame(captured, firstItem);

        Assertions.assertFalse(cartService.removeItem(1L, 3L));
        Assertions.assertTrue(cartService.removeItem(1L, 2L));
    }

    @Test
    void removeAllItemsFromCart() {
        Cart cart = getCart();
        BDDMockito.given(cartRepository.getCartByIdForUpdate(cart.getId())).willReturn(Optional.of(cart));
        BDDMockito.given(cartRepository.save(cart)).willReturn(cart);
        Assertions.assertDoesNotThrow(() -> cartService.removeAllItemsFromCart(1L));
        Assertions.assertEquals(0, cart.getCartItems().size());
    }

    @Test
    void addItemToCart() {
        Cart cart = getCart();
        Item itemToBeAdded = getNewItem();
        BDDMockito.given(cartRepository.findById(cart.getId())).willReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).then(AdditionalAnswers.returnsFirstArg());
        Assertions.assertDoesNotThrow(() -> cartService.addItemToCart(itemToBeAdded, 1L, 1));

        Optional<CartItem> cartItemOpt = cart.getCartItems().stream()
                .filter(ca -> ca.getItem().getId().equals(itemToBeAdded.getId()))
                .findFirst();
        Assertions.assertTrue(cartItemOpt.isPresent());
        CartItem cartItem = cartItemOpt.get();
        Assertions.assertEquals(1L, cartItem.getQuantity());
        cartService.addItemToCart(itemToBeAdded, 1L, 1);
        Assertions.assertEquals(2L, cartItem.getQuantity());
    }
}
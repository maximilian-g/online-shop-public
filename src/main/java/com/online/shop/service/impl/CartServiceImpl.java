package com.online.shop.service.impl;

import com.online.shop.dto.CartDto;
import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.entity.Cart;
import com.online.shop.entity.CartItem;
import com.online.shop.entity.Item;
import com.online.shop.repository.CartItemRepository;
import com.online.shop.repository.CartRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.CartService;
import com.online.shop.service.exception.NotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl extends BaseService implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public static final String SINGLE_CART_ATTRIBUTE_NAME = "cart";

    @Autowired
    protected CartServiceImpl(CartRepository cartRepository, Validator validator, CartItemRepository cartItemRepository) {
        super(validator, LoggerFactory.getLogger(CartServiceImpl.class));
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Collection<ItemQuantityDto> getItemsFromCart(Long cartId) {
        return getById(cartId).getCartItems();
    }

    @Override
    public void updateItemInCart(Long cartId, Long itemId, long quantityToAdd) {
        Cart cart = getCartByIdForUpdate(cartId);
        cart.getCartItems().stream().filter(ci -> ci.getItem().getId().equals(itemId)).forEach(ci -> {
            ci.setQuantity(ci.getQuantity() + quantityToAdd);
            cartItemRepository.save(ci);
        });
    }

    @Override
    public CartDto getById(Long id) {
        return CartDto.getCartDto(getCartById(id));
    }

    @Override
    public boolean removeItem(Long cartId, Long itemId) {
        Cart cart = getCartByIdForUpdate(cartId);
        List<CartItem> cartItemsToDelete = cart.getCartItems()
                .stream()
                .filter(ci -> ci.getCart().getId().equals(cartId) && ci.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
        cartItemsToDelete.forEach(cartItemRepository::delete);
        return cartItemsToDelete.size() > 0;
    }

    @Override
    public void removeAllItemsFromCart(Long cartId) {
        Cart cart = getCartByIdForUpdate(cartId);
        removeAllItemsFromCart(cart);
    }

    public void removeAllItemsFromCart(Cart cart) {
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCartById(Long id) {
        return cartRepository.findById(id).orElseThrow(
                () -> getCartNotFoundException(id)
        );
    }

    public Cart getCartByIdForUpdate(Long id) {
        return cartRepository.getCartByIdForUpdate(id).orElseThrow(
                () -> getCartNotFoundException(id)
        );
    }

    private NotFoundException getCartNotFoundException(Long id) {
        return new NotFoundException("Cart with id '" + id + "' not found.");
    }

    public void addItemToCart(Item item, Long cartId, long quantity) {
        Cart cart = getCartById(cartId);
        if(cart.getCartItems().stream().noneMatch(i -> i.getId().getItemId().equals(item.getId()))) {
            CartItem cartItem = CartItem.CartItemBuilder.createBuilder()
                            .id(cart.getId(), item.getId())
                            .quantity(quantity)
                            .item(item)
                            .cart(cart)
                            .build();
            cartItem = cartItemRepository.save(cartItem);
            cart.getCartItems().add(cartItem);
        } else {
            cart.getCartItems().forEach((ci) -> {
                if (ci.getItem().getId().equals(item.getId())) {
                    ci.setQuantity(ci.getQuantity() + quantity);
                    cartItemRepository.save(ci);
                }
            });
        }
        cartRepository.save(cart);
    }
}

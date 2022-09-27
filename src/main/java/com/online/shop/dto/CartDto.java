package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.entity.Cart;

import java.util.Collection;
import java.util.stream.Collectors;

public class CartDto {

    private Long id;
    private Long ownerId;
    @JsonIgnore
    private String ownerUsername;
    private Collection<ItemQuantityDto> cartItems;

    private CartDto(Long id, Long ownerId, String ownerUsername, Collection<ItemQuantityDto> cartItems) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.cartItems = cartItems;
    }

    public static CartDto getCartDto(Cart cart) {
        Collection<ItemQuantityDto> items = cart.getCartItems()
                .stream()
                .map(ci -> new ItemQuantityDto(ItemDto.getItemDto(ci.getItem()), ci.getQuantity()))
                .collect(Collectors.toList());
        return new CartDto(cart.getId(), cart.getUser().getId(), cart.getUser().getUsername(), items);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Collection<ItemQuantityDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Collection<ItemQuantityDto> cartItems) {
        this.cartItems = cartItems;
    }
}

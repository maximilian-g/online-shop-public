package com.online.shop.entity;

import javax.persistence.*;

@Entity
@Table(name = "carts_items")
public class CartItem {

    @EmbeddedId
    private CartItemPrimaryKey id;

    // quantity of items that user wants to order
    private Long quantity;

    @ManyToOne
    @JoinColumn(name = "cart_id", updatable = false, insertable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "item_id", updatable = false, insertable = false)
    private Item item;

    public static class CartItemBuilder {

        private final CartItem cartItem;

        private CartItemBuilder() {
            cartItem = new CartItem();
        }

        public static CartItemBuilder createBuilder() {
            return new CartItemBuilder();
        }

        public CartItemBuilder id(Long cartId, Long itemId) {
            CartItemPrimaryKey cartItemPrimaryKey = new CartItemPrimaryKey();
            cartItemPrimaryKey.setCartId(cartId);
            cartItemPrimaryKey.setItemId(itemId);
            cartItem.setId(cartItemPrimaryKey);
            return this;
        }

        public CartItemBuilder quantity(Long quantity) {
            cartItem.setQuantity(quantity);
            return this;
        }

        public CartItemBuilder item(Item item) {
            cartItem.setItem(item);
            return this;
        }

        public CartItemBuilder cart(Cart cart) {
            cartItem.setCart(cart);
            return this;
        }

        public CartItem build() {
            return cartItem;
        }
    }

    public CartItemPrimaryKey getId() {
        return id;
    }

    public void setId(CartItemPrimaryKey id) {
        this.id = id;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}

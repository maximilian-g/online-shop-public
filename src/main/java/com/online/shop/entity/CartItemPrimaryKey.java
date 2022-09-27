package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class CartItemPrimaryKey implements Serializable {

    @Column(name = "cart_id")
    private Long cartId;
    @Column(name = "item_id")
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(obj instanceof CartItemPrimaryKey) {
            CartItemPrimaryKey key = (CartItemPrimaryKey) obj;
            return key.getItemId() != null && key.getCartId() != null &&
                    key.getItemId().equals(itemId) && key.getCartId().equals(cartId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int)(itemId ^ (itemId >>> 32));
        result = 31 * result + (int)(cartId ^ (cartId >>> 32));
        return result;
    }
}

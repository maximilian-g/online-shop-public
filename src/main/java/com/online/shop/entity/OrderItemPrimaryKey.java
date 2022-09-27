package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class OrderItemPrimaryKey implements Serializable {

    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "item_id")
    private Long itemId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(obj instanceof OrderItemPrimaryKey) {
            OrderItemPrimaryKey key = (OrderItemPrimaryKey) obj;
            return key.getItemId() != null && key.getOrderId() != null &&
                    key.getItemId().equals(itemId) && key.getOrderId().equals(orderId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int)(itemId ^ (itemId >>> 32));
        result = 31 * result + (int)(orderId ^ (orderId >>> 32));
        return result;
    }

}

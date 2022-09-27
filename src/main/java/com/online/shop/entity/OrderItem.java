package com.online.shop.entity;

import javax.persistence.*;

@Entity
@Table(name = "orders_items")
public class OrderItem {

    @EmbeddedId
    private OrderItemPrimaryKey id;

    private Long quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", updatable = false, insertable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", updatable = false, insertable = false)
    private Item item;

    public static class OrderItemBuilder {

        private final OrderItem orderItem;

        private OrderItemBuilder() {
            orderItem = new OrderItem();
        }

        public static OrderItemBuilder createBuilder() {
            return new OrderItemBuilder();
        }

        public OrderItemBuilder id(Long orderId, Long itemId) {
            OrderItemPrimaryKey orderItemPrimaryKey = new OrderItemPrimaryKey();
            orderItemPrimaryKey.setOrderId(orderId);
            orderItemPrimaryKey.setItemId(itemId);
            orderItem.setId(orderItemPrimaryKey);
            return this;
        }

        public OrderItemBuilder quantity(Long quantity) {
            orderItem.setQuantity(quantity);
            return this;
        }

        public OrderItemBuilder item(Item item) {
            orderItem.setItem(item);
            return this;
        }

        public OrderItemBuilder order(Order order) {
            orderItem.setOrder(order);
            return this;
        }

        public OrderItem build() {
            return orderItem;
        }
    }

    public OrderItemPrimaryKey getId() {
        return id;
    }

    public void setId(OrderItemPrimaryKey id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}

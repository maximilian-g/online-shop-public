package com.online.shop.repository;

import com.online.shop.entity.OrderItem;
import com.online.shop.entity.OrderItemPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPrimaryKey> {
}

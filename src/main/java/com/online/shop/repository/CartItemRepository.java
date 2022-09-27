package com.online.shop.repository;

import com.online.shop.entity.CartItem;
import com.online.shop.entity.CartItemPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemPrimaryKey> {
}

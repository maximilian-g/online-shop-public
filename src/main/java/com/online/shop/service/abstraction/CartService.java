package com.online.shop.service.abstraction;

import com.online.shop.dto.CartDto;
import com.online.shop.dto.ItemQuantityDto;

import java.util.Collection;

public interface CartService {

    boolean removeItem(Long cartId, Long itemId);

    void removeAllItemsFromCart(Long cartId);

    CartDto getById(Long id);

    Collection<ItemQuantityDto> getItemsFromCart(Long cartId);

    void updateItemInCart(Long cartId, Long itemId, long quantityToAdd);

}

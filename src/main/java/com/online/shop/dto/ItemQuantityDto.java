package com.online.shop.dto;

import com.online.shop.service.abstraction.PriceService;

import java.math.BigDecimal;

public class ItemQuantityDto {

    private ItemDto item;
    private Long quantity;

    public ItemQuantityDto() {
    }

    public ItemQuantityDto(ItemDto item, Long quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getTotalFormatted() {
        if(item.getPrice() != null && item.getPrice().getPrice() != null) {
            return PriceService.formatDecimal(item.getPrice().getPrice().multiply(new BigDecimal(quantity)));
        }
        return "0.00";
    }

}

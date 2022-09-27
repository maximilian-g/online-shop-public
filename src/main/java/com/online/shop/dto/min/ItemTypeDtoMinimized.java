package com.online.shop.dto.min;

import com.online.shop.entity.ItemType;

public class ItemTypeDtoMinimized {

    private Long id;
    private String name;

    public static ItemTypeDtoMinimized getMinimizedDto(ItemType itemType) {
        ItemTypeDtoMinimized result = new ItemTypeDtoMinimized();
        result.id = itemType.getId();
        result.name = itemType.getName();
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

package com.online.shop.dto.min;

import com.online.shop.entity.Category;

import javax.validation.constraints.NotNull;

public class CategoryDtoMinimized {

    @NotNull
    private Long id;
    private String name;

    public static CategoryDtoMinimized getMinimizedCategory(Category category) {
        CategoryDtoMinimized result = new CategoryDtoMinimized();
        result.id = category.getId();
        result.name = category.getName();
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

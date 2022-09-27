package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.dto.min.ItemTypeDtoMinimized;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.Category;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryDto {

    @Null(groups = {New.class})
    @NotNull(groups = {Exists.class})
    private Long id;
    @NotNull(groups = {New.class, Exists.class}, message = "Category {app.validation.notnull.msg}")
    @Size(groups = {New.class, Exists.class}, min = 3, max = 45, message = "Category {app.validation.size.msg} but provided ${validatedValue}")
    private String category;
    private List<ItemTypeDtoMinimized> itemTypes = Collections.emptyList();
    @JsonIgnore
    private long itemsAssociated;

    public CategoryDto(Long id, String category) {
        this.id = id;
        this.category = category;
    }

    public CategoryDto(String category) {
        this.id = null;
        this.category = category;
    }

    public static CategoryDto getCategoryDto(Category category) {
        CategoryDto result = new CategoryDto(category.getId(), category.getName());
        result.setItemsAssociated(category.getAssociatedItemsCount());
        List<ItemTypeDtoMinimized> itemTypes = category.getItemTypes()
                .stream()
                .map(ItemTypeDtoMinimized::getMinimizedDto)
                .collect(Collectors.toList());
        result.setItemTypes(itemTypes);
        return result;
    }

    public static Collection<CategoryDto> getCategoryDtoCollection(Collection<Category> categories) {
        Collection<CategoryDto> result = new ArrayList<>(categories.size());
        for(Category category : categories) {
            result.add(getCategoryDto(category));
        }
        return result;
    }

    public long getItemsAssociated() {
        return itemsAssociated;
    }

    public void setItemsAssociated(long itemsAssociated) {
        this.itemsAssociated = itemsAssociated;
    }

    public List<ItemTypeDtoMinimized> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<ItemTypeDtoMinimized> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

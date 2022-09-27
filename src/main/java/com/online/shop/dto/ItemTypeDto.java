package com.online.shop.dto;

import com.online.shop.dto.min.CategoryDtoMinimized;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.ItemType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

public class ItemTypeDto {

    @Null(groups = {New.class})
    @NotNull(groups = {Exists.class})
    private Long itemTypeId;
    @NotNull
    @Size(min = 3, max = 255, message = "Item type name {app.validation.size.msg}, provided value '${validatedValue}'")
    private String name;
    @NotNull(groups = {New.class, Exists.class})
    @Valid
    private CategoryDtoMinimized category;
    private List<PropertyDto> properties;

    public static ItemTypeDto getItemTypeDto(ItemType type) {
        ItemTypeDto result = new ItemTypeDto();
        result.setItemTypeId(type.getId());
        result.setName(type.getName());
        result.setCategory(CategoryDtoMinimized.getMinimizedCategory(type.getCategory()));
        result.setProperties(type.getProperties().stream().map(PropertyDto::getPropertyDto).collect(Collectors.toList()));
        return result;
    }

    public static List<ItemTypeDto> getItemTypeDtoList(List<ItemType> types) {
        return types.stream().map(ItemTypeDto::getItemTypeDto).collect(Collectors.toList());
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryDtoMinimized getCategory() {
        return category;
    }

    public void setCategory(CategoryDtoMinimized category) {
        this.category = category;
    }

    public List<PropertyDto> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDto> properties) {
        this.properties = properties;
    }
}

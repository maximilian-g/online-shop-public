package com.online.shop.dto;

import com.online.shop.entity.Property;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyDto {

    private Long id;
    @NotNull
    private Long itemTypeId;
    @NotNull
    private String name;
    private List<PropertyValueDto> propertyValues;

    public static PropertyDto getPropertyDto(Property property) {
        PropertyDto result = new PropertyDto();
        result.id = property.getId();
        result.name = property.getName();
        result.itemTypeId = property.getItemType().getId();
        result.propertyValues = property
                .getPropertyValues()
                .stream()
                .map(PropertyValueDto::getPropertyValueDto)
                .collect(Collectors.toList());
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PropertyValueDto> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<PropertyValueDto> propertyValues) {
        this.propertyValues = propertyValues;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }
}

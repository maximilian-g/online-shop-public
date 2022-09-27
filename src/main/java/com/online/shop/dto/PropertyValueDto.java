package com.online.shop.dto;

import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.PropertyValue;

import javax.validation.constraints.NotNull;

public class PropertyValueDto {

    @NotNull(groups = {New.class, Exists.class})
    private Long propId;
    @NotNull(groups = {Exists.class})
    private Long propValueId;
    @NotNull
    private String value;

    public static PropertyValueDto getPropertyValueDto(PropertyValue propertyValue) {
        PropertyValueDto result = new PropertyValueDto();
        result.propValueId = propertyValue.getId();
        result.propId = propertyValue.getProperty().getId();
        result.value = propertyValue.getValue();
        return result;
    }

    public Long getPropValueId() {
        return propValueId;
    }

    public void setPropValueId(Long propValueId) {
        this.propValueId = propValueId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getPropId() {
        return propId;
    }

    public void setPropId(Long propId) {
        this.propId = propId;
    }
}

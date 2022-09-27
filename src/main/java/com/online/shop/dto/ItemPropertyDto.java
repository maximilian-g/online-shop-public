package com.online.shop.dto;

// represents a single property of an item
public class ItemPropertyDto {

    private Long propNameId;
    private Long propValueId;
    private String name;
    private String value;

    public ItemPropertyDto() {
    }

    public ItemPropertyDto(Long propNameId, String name, Long propValueId, String value) {
        this.propNameId = propNameId;
        this.propValueId = propValueId;
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getPropNameId() {
        return propNameId;
    }

    public void setPropNameId(Long propNameId) {
        this.propNameId = propNameId;
    }

    public Long getPropValueId() {
        return propValueId;
    }

    public void setPropValueId(Long propValueId) {
        this.propValueId = propValueId;
    }

    @Override
    public String toString() {
        return "ItemPropertyDto{" +
                "propNameId=" + propNameId +
                ", propValueId=" + propValueId +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

package com.online.shop.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "items_properties")
public class ItemPropertyValueEntity {

    @EmbeddedId
    private ItemPropertyValuePrimaryKey id;

    @ManyToOne
    @JoinColumn(name = "property_value_id", updatable = false, insertable = false)
    private PropertyValue propertyValue;

    @ManyToOne
    @JoinColumn(name = "item_id", updatable = false, insertable = false)
    private Item item;

    public ItemPropertyValuePrimaryKey getId() {
        return id;
    }

    public void setId(ItemPropertyValuePrimaryKey id) {
        this.id = id;
    }

    public PropertyValue getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(PropertyValue propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}

package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ItemPropertyValuePrimaryKey implements Serializable {

    @Column(name = "property_value_id")
    private Long propertyValueId;
    @Column(name = "item_id")
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPropertyValueId() {
        return propertyValueId;
    }

    public void setPropertyValueId(Long propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(obj instanceof ItemPropertyValuePrimaryKey) {
            ItemPropertyValuePrimaryKey key = (ItemPropertyValuePrimaryKey) obj;
            return key.getItemId() != null && key.getPropertyValueId() != null &&
                    key.getItemId().equals(itemId) && key.getPropertyValueId().equals(propertyValueId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (int)(itemId ^ (itemId >>> 32));
        result = 31 * result + (int)(propertyValueId ^ (propertyValueId >>> 32));
        return result;
    }

}

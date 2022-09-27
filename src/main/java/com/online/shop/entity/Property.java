package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    @Column(name = "property_name")
    @NotNull
    @Size(min = 3, max = 254, message = "Property name length must be between 3 and 255")
    private String name;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private Collection<PropertyValue> propertyValues = new ArrayList<>();

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

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Collection<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(Collection<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }
}

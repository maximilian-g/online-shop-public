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
@Table(name = "property_values")
public class PropertyValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_value_id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "property_value")
    @NotNull
    @Size(min = 1, max = 254, message = "Property value length must be between 1 and 255")
    private String value;

    @OneToMany(mappedBy = "propertyValue", fetch = FetchType.LAZY)
    private Collection<ItemPropertyValueEntity> itemPropertyLink = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String propertyValue) {
        this.value = propertyValue;
    }

    public Collection<ItemPropertyValueEntity> getItemPropertyLink() {
        return itemPropertyLink;
    }

    public void setItemPropertyLink(Collection<ItemPropertyValueEntity> itemPropertyLink) {
        this.itemPropertyLink = itemPropertyLink;
    }
}

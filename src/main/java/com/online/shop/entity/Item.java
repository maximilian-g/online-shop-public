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
import java.util.Date;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false, unique = true)
    private Long id;

    @Column
    @NotNull(message = "Item name {app.validation.notnull.msg}")
    @Size(min = 3, max = 255, message = "Item name {app.validation.size.msg} but provided ${validatedValue}")
    private String name;

    @Column
    @Size(min = 3, max = 1024, message = "Description {app.validation.size.msg} but provided ${validatedValue}")
    private String description;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Collection<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Collection<Price> prices = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Collection<ItemPropertyValueEntity> propertyValues = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_type_id")
    private ItemType type;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Collection<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Collection<ItemQuantityChange> changes = new ArrayList<>();

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

    public Collection<OrderItem> getOrders() {
        return orderItems;
    }

    public void setOrders(Collection<OrderItem> orders) {
        this.orderItems = orders;
    }

    public Price getPriceForDate(Date date) {
        if(prices != null) {
            for (Price price : prices) {
                if (price.getStartDate().before(date) &&
                        (price.getEndDate() == null || price.getEndDate().after(date))) {
                    return price;
                }
            }
        }
        return null;
    }

    public Price getPrice() {
        Date currentDate = new Date();
        return getPriceForDate(currentDate);
    }

    public Collection<Price> getPrices() {
        return prices;
    }

    public void setPrices(Collection<Price> prices) {
        this.prices = prices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getQuantity() {
        return changes.stream().mapToLong(ItemQuantityChange::getChange).sum();
    }

    public Collection<Image> getImages() {
        return images;
    }

    public void setImages(Collection<Image> images) {
        this.images = images;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public Collection<ItemPropertyValueEntity> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(Collection<ItemPropertyValueEntity> propertyValues) {
        this.propertyValues = propertyValues;
    }

    public long getQuantityOn(Date date) {
        return changes.stream()
                .filter(c -> c.getChangeDate().before(date))
                .mapToLong(ItemQuantityChange::getChange)
                .sum();
    }

    public Collection<ItemQuantityChange> getChanges() {
        return changes;
    }

    public void setChanges(Collection<ItemQuantityChange> changes) {
        this.changes = changes;
    }
}

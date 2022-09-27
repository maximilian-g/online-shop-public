package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "category_name", unique = true)
    @NotNull(message = "Category {app.validation.notnull.msg}")
    @Size(min = 3, max = 45, message = "Category {app.validation.size.msg} but provided ${validatedValue}")
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<ItemType> itemTypes = new ArrayList<>();

    @Transient
    private long associatedItemsCount;

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

    public long getAssociatedItemsCount() {
        return associatedItemsCount;
    }

    public void setAssociatedItemsCount(long count) {
        associatedItemsCount = count;
    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }
}

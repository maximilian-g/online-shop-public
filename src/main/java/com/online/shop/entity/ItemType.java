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
@Table(name = "item_types")
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_type_id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "item_type_name")
    @NotNull
    @Size(min = 3, max = 254, message = "Name length of item type must be between 3 and 255")
    private String name;

    @OneToMany(mappedBy = "itemType", fetch = FetchType.LAZY)
    private Collection<Property> properties = new ArrayList<>();

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Collection<Property> getProperties() {
        return properties;
    }

    public void setProperties(Collection<Property> properties) {
        this.properties = properties;
    }
}

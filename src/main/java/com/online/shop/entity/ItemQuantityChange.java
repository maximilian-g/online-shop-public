package com.online.shop.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "items_quantity_changes")
public class ItemQuantityChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "change_value")
    @NotNull
    private Long change;

    @Column
    @NotNull
    private Date changeDate;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChange() {
        return change;
    }

    public void setChange(Long change) {
        this.change = change;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public ItemQuantityChange setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
        return this;
    }
}

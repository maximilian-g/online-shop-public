package com.online.shop.entity;

import com.online.shop.util.DateUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "prices")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "start_date")
    @NotNull(message = "Start date {app.validation.notnull.msg}")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column
    @NotNull(message = "Price {app.validation.notnull.msg}")
    @Positive(message = "{app.validation.positive.msg}")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getFormattedStartDate() {
        return DateUtil.formatDate(startDate);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFormattedEndDate() {
        return endDate != null ?
                DateUtil.formatDate(endDate) : "";
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "{ " +
                "id = " + id + ", " +
                "startDate = " + startDate + ", " +
                "endDate = " + endDate + ", " +
                "price = " + price +
                " }";
    }
}

package com.online.shop.entity;

import com.online.shop.util.DateUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "start_date")
    @NotNull(message = "Order start date {app.validation.notnull.msg}")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column
    private boolean paid;

    @Column
    private String paymentId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Order status {app.validation.notnull.msg}")
    private OrderStatus status;

    @Column
    @NotNull(message = "Order description {app.validation.notnull.msg}")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Collection<OrderItem> orderItems = new ArrayList<>();

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
        return endDate != null ? DateUtil.formatDate(endDate)
                : "";
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Collection<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Collection<OrderItem> items) {
        this.orderItems = items;
    }

    public static Order copy(Order order) {
        Order resultOrder = new Order();
        resultOrder.setId(order.id);
        resultOrder.setStartDate(order.startDate);
        resultOrder.setEndDate(order.endDate);
        resultOrder.setPaid(order.paid);
        resultOrder.setOrderItems(List.copyOf(order.getOrderItems()));
        resultOrder.setUser(order.getUser());
        resultOrder.setAddress(order.getAddress());
        return resultOrder;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}

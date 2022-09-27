package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.entity.Order;
import com.online.shop.entity.Price;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.util.DateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class OrderDto {

    private Long id;
    private Date startDate;
    private Date endDate;
    private boolean isCompleted;
    private boolean isPaid;
    private String status;
    private String description;
    private AddressDto address;
    private Collection<ItemQuantityDto> orderItems;
    private BigDecimal total;
    private String ownerUsername;
    @JsonIgnore
    private String paymentId;

    public OrderDto(Long id,
                    Date startDate,
                    Date endDate,
                    boolean isCompleted,
                    boolean isPaid,
                    String status,
                    String description,
                    AddressDto address,
                    Collection<ItemQuantityDto> items,
                    BigDecimal total,
                    String ownerUsername,
                    String paymentId) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = isCompleted;
        this.isPaid = isPaid;
        this.status = status;
        this.description = description;
        this.address = address;
        this.orderItems = items;
        this.total = total;
        this.ownerUsername = ownerUsername;
        this.paymentId = paymentId;
    }

    public static OrderDto getOrderDto(Order order) {
        Collection<ItemQuantityDto> itemQuantityDtoCollection = order.getOrderItems()
                .stream()
                .map(oi -> {
                    ItemDto dto = ItemDto.getItemDto(oi.getItem(), order.getStartDate());
                    dto.getItemType().setProperties(null);
                    Price itemPrice = oi.getItem().getPriceForDate(order.getStartDate());
                    PriceDto price = null;
                    if(itemPrice != null) {
                        price = PriceDto.getPriceDto(itemPrice);
                    }
                    dto.setPrice(price);
                    return new ItemQuantityDto(dto, oi.getQuantity());
                })
                .collect(Collectors.toList());
        BigDecimal total = new BigDecimal("0.0");
        for(ItemQuantityDto itemQuantityDto : itemQuantityDtoCollection) {
            if(itemQuantityDto.getItem().getPrice() != null) {
                total = total.add(itemQuantityDto.getItem().getPrice().getPrice().multiply(new BigDecimal(itemQuantityDto.getQuantity())));
            }
        }
        return new OrderDto(order.getId(),
                order.getStartDate(),
                order.getEndDate(),
                order.isCompleted(),
                order.isPaid(),
                order.getStatus().getStatus(),
                order.getDescription(),
                AddressDto.getAddressDto(order.getAddress()),
                itemQuantityDtoCollection,
                total,
                order.getUser().getUsername(),
                order.getPaymentId());
    }

    public static Collection<OrderDto> getOrderDtoCollection(Collection<Order> orders) {
        Collection<OrderDto> result = new ArrayList<>(orders.size());
        for(Order order : orders) {
            result.add(getOrderDto(order));
        }
        return result;
    }

    @JsonIgnore
    public String getFormattedStartDate() {
        return DateUtil.formatDate(startDate);
    }

    @JsonIgnore
    public String getFormattedEndDate() {
        return endDate != null ? DateUtil.formatDate(endDate) : "";
    }

    @JsonIgnore
    public int getCalculatedItemSize() {
        int res = 0;
        for(ItemQuantityDto dto : getOrderItems()) {
            res += dto.getQuantity();
        }
        return res;
    }

    @JsonIgnore
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public Collection<ItemQuantityDto> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Collection<ItemQuantityDto> orderItems) {
        this.orderItems = orderItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalFormatted() {
        return total != null ? PriceService.formatDecimal(total) : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

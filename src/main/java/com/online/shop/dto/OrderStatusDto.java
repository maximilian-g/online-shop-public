package com.online.shop.dto;

import javax.validation.constraints.NotNull;

public class OrderStatusDto {

    @NotNull
    private String status;
    private String description;

    public String getStatus() {
        return status;
    }

    public OrderStatusDto setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public OrderStatusDto setDescription(String description) {
        this.description = description;
        return this;
    }
}

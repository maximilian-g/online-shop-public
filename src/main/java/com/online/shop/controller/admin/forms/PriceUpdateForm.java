package com.online.shop.controller.admin.forms;

import java.math.BigDecimal;

public class PriceUpdateForm {
    public final String startDate;
    public final String endDate;
    public final BigDecimal price;

    public PriceUpdateForm(String startDate, String endDate, BigDecimal price) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }
}

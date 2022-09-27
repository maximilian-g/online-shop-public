package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.Price;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.util.DateUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;

public class PriceDto {

    @Null(groups = {New.class})
    private Long id;
    @NotNull
    private Long itemId;
    @JsonIgnore
    private String itemName;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date endDate;
    @NotNull
    @Positive
    private BigDecimal price;

    public static PriceDto getPriceDto(Price price) {
        PriceDto result = new PriceDto();
        result.setId(price.getId());
        result.setItemId(price.getItem() == null ? null : price.getItem().getId());
        result.setItemName(price.getItem() == null ? null : price.getItem().getName());
        result.setStartDate(price.getStartDate());
        result.setEndDate(price.getEndDate());
        result.setPrice(price.getPrice());
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

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPriceFormatted() {
        return price != null ? PriceService.formatDecimal(price) : "";
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

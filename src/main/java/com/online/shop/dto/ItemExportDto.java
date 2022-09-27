package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.online.shop.entity.Item;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ItemExportDto extends ItemDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PriceDto> priceHistory;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long quantityOnExportMoment;

    public static ItemExportDto getItemExportDto(Item item, Date priceDate) {
        ItemExportDto result = new ItemExportDto();
        result.setQuantityOnExportMoment(item.getQuantityOn(priceDate));
        result.setPriceHistory(item.getPrices().stream().map(PriceDto::getPriceDto).collect(Collectors.toList()));
        // filling data with ItemDto's fields
        ItemDto.getItemDto(item, priceDate, result);
        // export DTO already has price history, there is no need to add it as separate field
        result.setPrice(null);
        return result;
    }

    public List<PriceDto> getPriceHistory() {
        return priceHistory;
    }

    public ItemExportDto setPriceHistory(List<PriceDto> priceHistory) {
        this.priceHistory = priceHistory;
        return this;
    }

    public Long getQuantityOnExportMoment() {
        return quantityOnExportMoment;
    }

    public ItemExportDto setQuantityOnExportMoment(Long quantityOnExportMoment) {
        this.quantityOnExportMoment = quantityOnExportMoment;
        return this;
    }
}

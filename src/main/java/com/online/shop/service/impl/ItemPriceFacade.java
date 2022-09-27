package com.online.shop.service.impl;

import com.online.shop.controller.admin.forms.PriceUpdateForm;
import com.online.shop.dto.PriceDto;
import com.online.shop.entity.Price;
import com.online.shop.service.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ItemPriceFacade {

    private final ItemServiceImpl itemService;
    private final PriceServiceImpl priceService;

    @Autowired
    public ItemPriceFacade(ItemServiceImpl itemService, PriceServiceImpl priceService) {
        this.itemService = itemService;
        this.priceService = priceService;
    }


    @Transactional(rollbackFor = {BusinessException.class})
    public Long createPriceAndAttachToItem(PriceDto priceDto) {
        Price price = new Price();
        price.setStartDate(priceDto.getStartDate());
        price.setEndDate(priceDto.getEndDate());
        price.setPrice(priceDto.getPrice());
        price = priceService.createPriceEntity(price);
        itemService.attachPriceToItemAndGetItem(priceDto.getItemId(), price.getId());
        price.setItem(itemService.getItemById(priceDto.getItemId()));
        return priceService.updatePriceEntity(price).getId();
    }

    @Transactional(rollbackFor = {BusinessException.class})
    public Price createPrice(PriceDto priceDto) {
        Price price = new Price();
        price.setStartDate(priceDto.getStartDate());
        price.setEndDate(priceDto.getEndDate());
        price.setPrice(priceDto.getPrice());
        return priceService.createPriceEntity(price);
    }

    @Transactional(rollbackFor = {BusinessException.class})
    public Long updatePriceAndReattachToItem(PriceDto priceDto) {
        Price price = priceService.getPriceByIdForUpdate(priceDto.getId());
        price.setStartDate(priceDto.getStartDate());
        price.setEndDate(priceDto.getEndDate());
        price.setPrice(priceDto.getPrice());
        if(price.getItem() == null || !price.getItem().getId().equals(priceDto.getItemId())) {
            itemService.attachPriceToItemAndGetItem(priceDto.getItemId(), price.getId());
            price.setItem(itemService.getItemById(priceDto.getItemId()));
        }
        return priceService.updatePriceEntity(price).getId();
    }

    @Transactional(rollbackFor = {BusinessException.class})
    public Long updatePrice(Long id, Long itemId, boolean reassign, String startDate, String endDate, BigDecimal price) {
        Price priceEntity = priceService.getPriceByIdForUpdate(id);
        priceEntity = priceService.updatePriceFieldsByForm(priceEntity,
                new PriceUpdateForm(startDate, endDate, price));
        if (reassign) {
            priceEntity.setItem(itemService.attachPriceToItemEntityAndGetItem(itemId, priceEntity.getId()));
        }
        priceEntity = priceService.updatePriceEntity(priceEntity);
        return priceEntity.getId();
    }

}

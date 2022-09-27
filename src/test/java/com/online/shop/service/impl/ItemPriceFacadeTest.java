package com.online.shop.service.impl;

import com.online.shop.controller.admin.forms.PriceUpdateForm;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.PriceDto;
import com.online.shop.entity.Item;
import com.online.shop.entity.Price;
import com.online.shop.util.DateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class ItemPriceFacadeTest {

    @Mock
    private ItemServiceImpl itemService;
    @Mock
    private PriceServiceImpl priceService;

    private ItemPriceFacade itemPriceFacade;

    @BeforeEach
    void init() {
        itemPriceFacade = new ItemPriceFacade(itemService, priceService);
    }

    private PriceDto getNewPrice() {
        PriceDto price = new PriceDto();
        price.setStartDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(25)));
        price.setEndDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15)));
        price.setPrice(new BigDecimal("25.45"));
        price.setItemId(1L);
        return price;
    }

    private PriceDto getExistentPrice() {
        PriceDto price = getNewPrice();
        price.setId(1L);
        return price;
    }

    private Price getPriceEntity() {
        Price price = new Price();
        price.setId(1L);
        price.setStartDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(25)));
        price.setEndDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15)));
        price.setPrice(new BigDecimal("25.45"));
        return price;
    }

    @Test
    void createPriceAndAttachToItem() {
        PriceDto priceDto = getNewPrice();

        BDDMockito.given(itemService.attachPriceToItemAndGetItem(any(Long.class), any(Long.class))).willReturn(new ItemDto());
        when(priceService.createPriceEntity(any(Price.class))).then((Answer<Price>) invocationOnMock -> {
            Price priceEntity = invocationOnMock.getArgument(0);
            priceEntity.setId(1L);
            return priceEntity;
        });
        BDDMockito.given(itemService.getItemById(any(Long.class))).willReturn(new Item());
        when(priceService.updatePriceEntity(any(Price.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> itemPriceFacade.createPriceAndAttachToItem(priceDto));

        ArgumentCaptor<Price> categoryArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing an item
        Mockito.verify(priceService).createPriceEntity(categoryArgumentCaptor.capture());
        Price captured = categoryArgumentCaptor.getValue();
        Assertions.assertEquals(priceDto.getStartDate().getTime(), captured.getStartDate().getTime());
        Assertions.assertEquals(priceDto.getEndDate().getTime(), captured.getEndDate().getTime());
        Assertions.assertEquals(priceDto.getPrice(), captured.getPrice());

    }

    @Test
    void createPrice() {
        PriceDto priceDto = getNewPrice();
        when(priceService.createPriceEntity(any(Price.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> itemPriceFacade.createPrice(priceDto));

        ArgumentCaptor<Price> categoryArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing an item
        Mockito.verify(priceService).createPriceEntity(categoryArgumentCaptor.capture());
        Price captured = categoryArgumentCaptor.getValue();
        Assertions.assertEquals(priceDto.getStartDate().getTime(), captured.getStartDate().getTime());
        Assertions.assertEquals(priceDto.getEndDate().getTime(), captured.getEndDate().getTime());
        Assertions.assertEquals(priceDto.getPrice(), captured.getPrice());
    }

    @Test
    void updatePriceAndReattachToItem() {
        Price price = getPriceEntity();
        PriceDto existentPriceDto = getExistentPrice();
        when(priceService.updatePriceEntity(any(Price.class))).then(AdditionalAnswers.returnsFirstArg());
        BDDMockito.given(priceService.getPriceByIdForUpdate(price.getId())).willReturn(price);
        BDDMockito.given(itemService.attachPriceToItemAndGetItem(any(Long.class), any(Long.class))).willReturn(new ItemDto());
        BDDMockito.given(itemService.getItemById(any(Long.class))).willReturn(new Item());

        Assertions.assertDoesNotThrow(() -> itemPriceFacade.updatePriceAndReattachToItem(existentPriceDto));
    }

    @Test
    void updatePrice() {

        Price price = getPriceEntity();
        PriceDto existentPriceDto = getExistentPrice();
        when(priceService.updatePriceEntity(any(Price.class))).then(AdditionalAnswers.returnsFirstArg());
        BDDMockito.given(priceService.getPriceByIdForUpdate(price.getId())).willReturn(price);
        BDDMockito.given(itemService.attachPriceToItemEntityAndGetItem(any(Long.class), any(Long.class))).willReturn(new Item());
        when(priceService.updatePriceFieldsByForm(any(Price.class), any(PriceUpdateForm.class))).then((Answer<Price>) invocationOnMock -> {
            Price priceEntity = invocationOnMock.getArgument(0);
            PriceUpdateForm form = invocationOnMock.getArgument(1);
            priceEntity.setStartDate(DateUtil.getDateFromString(form.startDate));
            priceEntity.setEndDate(DateUtil.getDateFromString(form.endDate));
            priceEntity.setPrice(form.price);
            return priceEntity;
        });

        Assertions.assertDoesNotThrow(() ->
                itemPriceFacade.updatePrice(
                        existentPriceDto.getId(),
                        existentPriceDto.getItemId(),
                        false,
                        "2021-05-05",
                        "2021-06-05",
                        new BigDecimal("25.5")
                )
        );

        Assertions.assertDoesNotThrow(() ->
                itemPriceFacade.updatePrice(
                        existentPriceDto.getId(),
                        existentPriceDto.getItemId(),
                        true,
                        "2021-05-05",
                        "2021-06-05",
                        new BigDecimal("25.5")
                )
        );

    }




}
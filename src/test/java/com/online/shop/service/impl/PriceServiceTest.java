package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.controller.admin.forms.PriceUpdateForm;
import com.online.shop.entity.Item;
import com.online.shop.entity.Order;
import com.online.shop.entity.OrderItem;
import com.online.shop.entity.Price;
import com.online.shop.repository.PriceRepository;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.exception.PriceCreationException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    private static final int PAGE_SIZE_PROP = 8;

    @Mock
    private PriceRepository priceRepository;

    private PriceServiceImpl priceService;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        PaginationConfig paginationConfig = new PaginationConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", PAGE_SIZE_PROP, true);
        priceService = new PriceServiceImpl(
                validator,
                priceRepository,
                paginationConfig);
    }

    @Test
    void getAllPrices() {
        // we need to verify that we are invoking mock's methods
        priceService.getAllPrices();
        Mockito.verify(priceRepository).findAll();
    }

    @Test
    void getPriceById() {
        Long id = 1L;
        Price priceToBeReturned = new Price();
        priceToBeReturned.setId(id);
        priceToBeReturned.setStartDate(new java.util.Date(System.currentTimeMillis()));
        priceToBeReturned.setEndDate(new java.util.Date(priceToBeReturned.getStartDate().getTime() + 7200000L));
        priceToBeReturned.setPrice(new BigDecimal("25.00"));
        // mock will return needed price by id 1
        BDDMockito.given(priceRepository.findById(id)).willReturn(Optional.of(priceToBeReturned));

        AtomicReference<Price> priceFromService = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> priceFromService.set(priceService.getPriceById(id)));
        Assertions.assertDoesNotThrow(() -> priceService.getById(id));

        Assertions.assertSame(priceToBeReturned, priceFromService.get());
        Assertions.assertThrows(NotFoundException.class, () -> priceService.getPriceById(-1L));

    }

    @Test
    void shouldNotThrowExceptionOnCreateAndPricesMustBeSame() {
        // must not throw an exception
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        BDDMockito.given(priceRepository.saveAndFlush(price)).willReturn(price);
        Assertions.assertDoesNotThrow(() -> priceService.createPrice(price));

        // testing if mock was invoked saveAndFlush method with exactly our price object
        ArgumentCaptor<Price> priceArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing a price
        Mockito.verify(priceRepository).saveAndFlush(priceArgumentCaptor.capture());
        Price captured = priceArgumentCaptor.getValue();
        Assertions.assertSame(captured, price);
    }

    @Test
    void shouldThrowExceptionOnCreation() {
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() - 1L));
        price.setPrice(new BigDecimal("25.00"));
        Assertions.assertThrows(PriceCreationException.class, () -> priceService.createPrice(price));

        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 2000L));
        price.setPrice(new BigDecimal("-25.00"));
        Assertions.assertThrows(PriceCreationException.class, () -> priceService.createPrice(price));

        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 2000L));
        price.setStartDate(null);
        price.setPrice(new BigDecimal("25.00"));
        Assertions.assertThrows(PriceCreationException.class, () -> priceService.createPrice(price));
    }

    @Test
    void shouldNotThrowExceptionOnUpdate() {
        // must not throw an exception
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        BDDMockito.given(priceRepository.saveAndFlush(price)).willReturn(price);
        Assertions.assertDoesNotThrow(() -> priceService.updatePrice(price));

        // testing if mock was invoked saveAndFlush method with exactly our price object
        ArgumentCaptor<Price> priceArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing a price
        Mockito.verify(priceRepository).saveAndFlush(priceArgumentCaptor.capture());
        Price captured = priceArgumentCaptor.getValue();
        Assertions.assertSame(captured, price);
    }

    @Test
    void shouldThrowExceptionOnUpdate() {
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() - 1L));
        price.setPrice(new BigDecimal("25.00"));
        Assertions.assertThrows(EntityUpdateException.class, () -> priceService.updatePrice(price));

        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 2000L));
        price.setPrice(new BigDecimal("-25.00"));
        Assertions.assertThrows(EntityUpdateException.class, () -> priceService.updatePrice(price));

        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 2000L));
        price.setStartDate(null);
        price.setPrice(new BigDecimal("25.00"));
        Assertions.assertThrows(EntityUpdateException.class, () -> priceService.updatePrice(price));
    }

    @Test
    void areCompatible() {
        Price pr1 = new Price();
        pr1.setStartDate(Date.valueOf(LocalDate.of(2020, 4, 1)));
        pr1.setEndDate(Date.valueOf(LocalDate.of(2020, 5, 1)));
        Price pr2 = new Price();
        pr2.setStartDate(Date.valueOf(LocalDate.of(2020, 5, 2)));
        pr2.setEndDate(Date.valueOf(LocalDate.of(2020, 6, 1)));
        assertTrue(PriceServiceImpl.areCompatible(pr1, pr2));
        assertTrue(PriceServiceImpl.areCompatible(pr2, pr1));

        pr1.setStartDate(Date.valueOf(LocalDate.of(2020, 4, 1)));
        pr1.setEndDate(Date.valueOf(LocalDate.of(2020, 5, 1)));
        pr2.setStartDate(Date.valueOf(LocalDate.of(2020, 5, 2)));
        pr2.setEndDate(null);
        assertTrue(PriceServiceImpl.areCompatible(pr1, pr2));
        assertTrue(PriceServiceImpl.areCompatible(pr2, pr1));

        pr1.setStartDate(Date.valueOf(LocalDate.of(2020, 5, 1)));
        pr1.setEndDate(Date.valueOf(LocalDate.of(2020, 6, 1)));
        pr2.setStartDate(Date.valueOf(LocalDate.of(2020, 5, 5)));
        pr2.setEndDate(null);
        assertFalse(PriceServiceImpl.areCompatible(pr1, pr2));
        assertFalse(PriceServiceImpl.areCompatible(pr2, pr1));

        pr1.setStartDate(Date.valueOf(LocalDate.of(2020, 5, 5)));
        pr1.setEndDate(null);
        pr2.setStartDate(Date.valueOf(LocalDate.of(2020, 4, 1)));
        pr2.setEndDate(null);
        assertFalse(PriceServiceImpl.areCompatible(pr1, pr2));
        assertFalse(PriceServiceImpl.areCompatible(pr2, pr1));
    }

    @Test
    public void detachFromItemShouldDetachItem() {
        // must not throw an exception
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);
        Item item = new Item();
        item.setId(2L);
        item.setName("Tennis rocket");
//        item.setQuantity(5000L);
        item.setOrders(Collections.emptyList());
        price.setItem(item);
        BDDMockito.given(priceRepository.save(price)).willReturn(price);
        BDDMockito.given(priceRepository.findById(price.getId())).willReturn(Optional.of(price));
        Assertions.assertDoesNotThrow(() -> priceService.detachItemFromPrice(price.getId()));
        assertNull(price.getItem());
    }

    @Test
    public void detachFromItemShouldDoNothing() {
        // must not throw an exception
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);
        // item will be null, so detachItemFromPriceEntity must do nothing
        BDDMockito.given(priceRepository.findById(price.getId())).willReturn(Optional.of(price));
        Assertions.assertDoesNotThrow(() -> priceService.detachItemFromPrice(price.getId()));
        assertNull(price.getItem());
    }

    @Test
    public void detachFromItemShouldThrowException() {
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);
        Item item = new Item();
        item.setId(2L);
        item.setName("Tennis rocket");
//        item.setQuantity(5000L);
        Order order = new Order();
        order.setId(1L);
        order.setStartDate(new java.sql.Date(price.getStartDate().getTime() + 1000));
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(OrderItem.OrderItemBuilder.createBuilder()
                .id(order.getId(), item.getId())
                .quantity(1L)
                .order(order)
                .item(item).build());
        order.setOrderItems(orderItemList);
        item.setOrders(orderItemList);
        price.setItem(item);
        item.setPrices(List.of(price));
        BDDMockito.given(priceRepository.findById(price.getId())).willReturn(Optional.of(price));
        Assertions.assertThrows(EntityUpdateException.class, () -> priceService.detachItemFromPrice(price.getId()));
        assertNotNull(price.getItem());
    }

    @Test
    public void removeAllPricesFromItemShouldNotThrow() {
        // must not throw an exception
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);

        Item item = new Item();
        item.setId(2L);
        item.setName("Tennis rocket");
//        item.setQuantity(5000L);
        item.setPrices(List.of(price));

        price.setItem(item);

        BDDMockito.given(priceRepository.saveAndFlush(price)).willReturn(price);
        Assertions.assertDoesNotThrow(() -> priceService.removeAllPricesFromItem(item));

        // testing if mock was invoked saveAndFlush method with exactly our price object
        ArgumentCaptor<Price> priceArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing a price
        Mockito.verify(priceRepository).saveAndFlush(priceArgumentCaptor.capture());
        Price captured = priceArgumentCaptor.getValue();
        Assertions.assertSame(captured, price);
    }

    @Test
    public void deleteByIdShouldThrowException() {
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);

        Item item = new Item();
        item.setId(2L);
        item.setName("Tennis rocket");
//        item.setQuantity(5000L);

        Order order = new Order();
        order.setId(1L);
        order.setStartDate(new java.sql.Date(price.getStartDate().getTime() + 1000));
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(OrderItem.OrderItemBuilder.createBuilder()
                .id(order.getId(), item.getId())
                .quantity(1L)
                .order(order)
                .item(item).build());
        order.setOrderItems(orderItemList);

        item.setOrders(orderItemList);
        price.setItem(item);
        item.setPrices(List.of(price));

        BDDMockito.given(priceRepository.findById(price.getId())).willReturn(Optional.of(price));
        Assertions.assertThrows(EntityUpdateException.class, () -> priceService.deletePriceById(price.getId()));
        assertNotNull(price.getItem());
    }

    @Test
    public void deleteByIdShouldNotThrowException() {
        Price price = new Price();
        price.setStartDate(new java.util.Date(System.currentTimeMillis()));
        price.setEndDate(new java.util.Date(price.getStartDate().getTime() + 7200000L));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);
        BDDMockito.given(priceRepository.findById(price.getId())).willReturn(Optional.of(price));
        Assertions.assertDoesNotThrow(() -> priceService.deletePriceById(price.getId()));

        ArgumentCaptor<Price> priceArgumentCaptor = ArgumentCaptor.forClass(Price.class);
        // capturing a price
        Mockito.verify(priceRepository).delete(priceArgumentCaptor.capture());
        Price captured = priceArgumentCaptor.getValue();
        Assertions.assertSame(captured, price);
    }

    @Test
    public void testUpdateByForm() throws ParseException {
        Price price = new Price();
        price.setStartDate(new Date(System.currentTimeMillis()));
        price.setEndDate(new Date(price.getStartDate().getTime() + TimeUnit.DAYS.toMillis(20)));
        price.setPrice(new BigDecimal("25.00"));
        price.setId(1L);

        Item item = new Item();
        item.setId(2L);
        item.setName("Tennis rocket");

        Order order = new Order();
        order.setId(1L);
        order.setStartDate(new Date(price.getStartDate().getTime() + TimeUnit.DAYS.toMillis(10)));
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(OrderItem.OrderItemBuilder.createBuilder()
                .id(order.getId(), item.getId())
                .quantity(1L)
                .order(order)
                .item(item).build());
        order.setOrderItems(orderItemList);

        item.setOrders(orderItemList);
        price.setItem(item);
        item.setPrices(List.of(price));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // will not throw an exception, because start date of price is EARLIER than order date,
        // order date is included in this time period
        PriceUpdateForm form = new PriceUpdateForm("2021-10-10", null, new BigDecimal("100.00"));
        PriceUpdateForm finalForm = form;
        Assertions.assertDoesNotThrow(() -> priceService.updatePriceFieldsByForm(price, finalForm));

        // price start date will be 1 day later than order date,
        // order will not be included in price period
        form = new PriceUpdateForm(
                formatter.format(new Date(order.getStartDate().getTime() + TimeUnit.DAYS.toMillis(1))),
                null,
                new BigDecimal("100.00")
        );
        PriceUpdateForm finalForm1 = form;
        Assertions.assertThrows(EntityUpdateException.class,
                () -> priceService.updatePriceFieldsByForm(price, finalForm1));

        // price end date will be 1 day earlier than order date,
        // order will not be included in price period
        form = new PriceUpdateForm(
                formatter.format(price.getStartDate()),
                formatter.format(new Date(order.getStartDate().getTime() - TimeUnit.DAYS.toMillis(1))),
                new BigDecimal("100.00")
        );
        PriceUpdateForm finalForm2 = form;
        Assertions.assertThrows(EntityUpdateException.class,
                () -> priceService.updatePriceFieldsByForm(price, finalForm2));

        // will throw an exception, because period of price does not intersect order date,
        // order date is not included in this time period
        form = new PriceUpdateForm("2021-10-10", "2021-12-12", new BigDecimal("100.00"));
        PriceUpdateForm finalForm3 = form;
        Assertions.assertThrows(EntityUpdateException.class,
                () -> priceService.updatePriceFieldsByForm(price, finalForm3));
        price.setItem(null);

        // will not throw an exception, there is no item for this price
        form = new PriceUpdateForm("2021-10-10", "2021-12-12", new BigDecimal("777.00"));
        PriceUpdateForm finalForm4 = form;
        Assertions.assertDoesNotThrow(() -> priceService.updatePriceFieldsByForm(price, finalForm4));
        Assertions.assertEquals("2021-10-10", formatter.format(price.getStartDate()));
        Assertions.assertEquals("2021-12-12", formatter.format(price.getEndDate()));
        Assertions.assertEquals(new BigDecimal("777.00"), price.getPrice());

        // simple case with invalid date
        form = new PriceUpdateForm("20wgwe0-10", null, new BigDecimal("100.00"));
        PriceUpdateForm finalForm5 = form;
        Assertions.assertThrows(EntityUpdateException.class,
                () -> priceService.updatePriceFieldsByForm(price, finalForm5));


        price.setStartDate(new Date(order.getStartDate().getTime() - TimeUnit.DAYS.toMillis(1)));
        price.setEndDate(new Date(price.getStartDate().getTime() + TimeUnit.DAYS.toMillis(20)));

        Price anotherPrice = new Price();
        anotherPrice.setStartDate(formatter.parse("2020-01-01"));
        anotherPrice.setEndDate(new Date(price.getStartDate().getTime() - TimeUnit.DAYS.toMillis(2)));
        anotherPrice.setPrice(new BigDecimal("25.00"));
        anotherPrice.setId(2L);
        item.setPrices(List.of(price, anotherPrice));
        price.setItem(item);
        anotherPrice.setItem(item);

        // must throw an exception, because price and anotherPrice are not compatible
        // (their periods intersects) and those start and end dates cannot be used
        // intersection will be in anotherPrice.getEndDate() and NEW price start date(current start date - 5 days)
        form = new PriceUpdateForm(
                formatter.format(new Date(price.getStartDate().getTime() - TimeUnit.DAYS.toMillis(5))),
                formatter.format(price.getEndDate()),
                new BigDecimal("777.00")
        );
        PriceUpdateForm finalForm6 = form;
        Assertions.assertThrows(EntityUpdateException.class,
                () -> priceService.updatePriceFieldsByForm(price, finalForm6));

    }

}
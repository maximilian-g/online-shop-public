package com.online.shop.service.impl;

import com.online.shop.config.AppConfig;
import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ImageDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.entity.Cart;
import com.online.shop.entity.Category;
import com.online.shop.entity.Image;
import com.online.shop.entity.Item;
import com.online.shop.entity.ItemQuantityChange;
import com.online.shop.entity.ItemType;
import com.online.shop.entity.OrderItem;
import com.online.shop.entity.Price;
import com.online.shop.repository.ItemQuantityChangeRepository;
import com.online.shop.repository.ItemRepository;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.ItemCreationException;
import com.online.shop.service.exception.NotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;
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

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemQuantityChangeRepository itemQuantityChangeRepository;

    private ItemServiceImpl itemService;

    @Mock
    private PriceServiceImpl priceService;
    @Mock
    private CartServiceImpl cartService;
    @Mock
    private ItemTypeServiceImpl itemTypeService;
    @Mock
    private TransactionHandler transactionHandler;

    private static final int PAGE_SIZE_PROP = 8;
    private static final String TIMEZONE = "Europe/Moscow";

    @BeforeEach
    void setUp() throws IllegalAccessException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        PaginationConfig paginationConfig = new PaginationConfig();
        AppConfig appConfig = new AppConfig();
        FieldUtils.writeField(paginationConfig, "pageSizeProp", PAGE_SIZE_PROP, true);
        FieldUtils.writeField(appConfig, "appTimezone", TIMEZONE, true);
        itemService = new ItemServiceImpl(itemRepository,
                validator,
                priceService,
                cartService,
                itemTypeService,
                itemQuantityChangeRepository, transactionHandler, paginationConfig,
                appConfig);
    }

    private Item getNormalItem() {
        Item item = new Item();
        item.setId(1L);
        ItemQuantityChange itemQuantityChange = new ItemQuantityChange();
        itemQuantityChange.setId(1L);
        itemQuantityChange.setItem(item);
        itemQuantityChange.setChange(500L);
        itemQuantityChange.setChangeDate(new Date());
        List<ItemQuantityChange> list = new ArrayList<>();
        list.add(itemQuantityChange);
        item.setChanges(list);
        item.setName("Keyboard");
        item.setDescription("Razer keyboard");

        Price price = new Price();
        price.setId(1L);
        price.setPrice(new BigDecimal("80.50"));
        price.setStartDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20)));
        price.setEndDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(20)));
        price.setItem(item);
        List<Price> prices = new ArrayList<>();
        prices.add(price);
        item.setPrices(prices);

        item.setOrders(Collections.emptyList());

        Category category = new Category();
        category.setId(1L);
        category.setName("Computer devices");
        category.setAssociatedItemsCount(item.getQuantity());

        ItemType type = new ItemType();
        type.setId(1L);
        type.setName("Keyboards");
        type.setCategory(category);
        category.setItemTypes(List.of(type));

        item.setType(type);

        item.setImages(new ArrayList<>());
        return item;
    }

    @Test
    void createItem() {
        Item item = getNormalItem();
        ItemDto dto = ItemDto.getItemDto(item);
        BDDMockito.given(itemTypeService.getItemTypeEntityById(item.getType().getId())).willReturn(item.getType());
        when(itemRepository.save(any(Item.class))).then(AdditionalAnswers.returnsFirstArg());
        dto = itemService.createItem(dto);
        Assertions.assertFalse(dto.isInStock());
        Assertions.assertNotNull(dto.getName());
        Assertions.assertNotNull(dto.getArticle());
        Assertions.assertNotNull(dto.getDescription());
        Assertions.assertNotNull(dto.getItemType());
        Assertions.assertNotNull(dto.getItemType().getItemTypeId());
        Assertions.assertNotNull(dto.getItemType().getName());
        Assertions.assertNotNull(dto.getItemType().getCategory());
        Assertions.assertNotNull(dto.getItemType().getCategory().getId());
        Assertions.assertNotNull(dto.getItemType().getCategory().getName());

        item.setName(null);
        ItemDto invalidDto = ItemDto.getItemDto(item);
        Assertions.assertThrows(ItemCreationException.class, () -> itemService.createItem(invalidDto));
    }

    @Test
    void updateItem() {
        Item item = getNormalItem();
        ItemDto dto = ItemDto.getItemDto(item);
        BDDMockito.given(itemTypeService.getItemTypeEntityById(item.getType().getId())).willReturn(item.getType());
        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        BDDMockito.given(itemRepository.getItemByIdForUpdate(item.getId())).willReturn(Optional.of(item));
        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(item.getId())).willReturn(item.getQuantity());
        when(itemRepository.save(any(Item.class))).then(AdditionalAnswers.returnsFirstArg());
        when(itemQuantityChangeRepository.saveAndFlush(any(ItemQuantityChange.class))).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(transactionHandler.runInNewTransaction(any(Supplier.class))).then((Answer<?>) invocationOnMock -> {
            Supplier<?> supplier = invocationOnMock.getArgument(0);
            return supplier.get();
        });

        dto = itemService.updateItem(dto, item.getQuantity());
        Assertions.assertTrue(dto.isInStock());
        Assertions.assertNotNull(dto.getName());
        Assertions.assertNotNull(dto.getDescription());
        Assertions.assertNotNull(dto.getItemType());
        Assertions.assertNotNull(dto.getItemType().getItemTypeId());
        Assertions.assertNotNull(dto.getItemType().getName());
        Assertions.assertNotNull(dto.getItemType().getCategory());
        Assertions.assertNotNull(dto.getItemType().getCategory().getId());
        Assertions.assertNotNull(dto.getItemType().getCategory().getName());

        dto = itemService.updateItem(dto, 5);
        Assertions.assertTrue(dto.isInStock());
        Assertions.assertNotNull(dto.getName());
        Assertions.assertNotNull(dto.getDescription());
        Assertions.assertNotNull(dto.getItemType());
        Assertions.assertNotNull(dto.getItemType().getItemTypeId());
        Assertions.assertNotNull(dto.getItemType().getName());
        Assertions.assertNotNull(dto.getItemType().getCategory());
        Assertions.assertNotNull(dto.getItemType().getCategory().getId());
        Assertions.assertNotNull(dto.getItemType().getCategory().getName());

        item.setName(null);
        ItemDto invalidDto = ItemDto.getItemDto(item);
        Assertions.assertThrows(EntityUpdateException.class, () -> itemService.updateItem(invalidDto, 20L));

        item.setName("Some name");
        item.getChanges().clear();
        ItemQuantityChange change = new ItemQuantityChange();
        change.setChangeDate(new Date());
        change.setChange(-50L);
        item.getChanges().add(change);
        ItemDto anotherInvalidDto = ItemDto.getItemDto(item);
        Assertions.assertThrows(EntityUpdateException.class, () -> itemService.updateItem(anotherInvalidDto, item.getQuantity()));
    }

    @Test
    void getById() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        ItemDto itemDto = itemService.getById(item.getId());
        Assertions.assertNotNull(itemDto.getId());
        Assertions.assertNotNull(itemDto.getItemType());
        Assertions.assertNotNull(itemDto.getItemType().getCategory());
        Assertions.assertNotNull(itemDto.getItemType().getCategory().getId());
        Assertions.assertNotNull(itemDto.getItemType().getCategory().getName());
        Assertions.assertNotNull(itemDto.getItemType().getName());
        Assertions.assertNotNull(itemDto.getItemType().getItemTypeId());
        Assertions.assertNotNull(itemDto.getDescription());
        Assertions.assertNotNull(itemDto.getName());
        Assertions.assertNotNull(itemDto.getPrice());
        Assertions.assertNotNull(itemDto.getPrice().getItemId());
        Assertions.assertNotNull(itemDto.getPrice().getPrice());
        Assertions.assertNotNull(itemDto.getPrice().getStartDate());
        Assertions.assertNotNull(itemDto.getPrice().getEndDate());
        Assertions.assertNotNull(itemDto.getPrice().getItemName());
    }

    @Test
    void addItemToCart() {
        Item item = getNormalItem();
        Cart cart = new Cart();
        cart.setId(1L);

        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(item.getId())).willReturn(item.getQuantity());
        doNothing().when(cartService).addItemToCart(any(Item.class), any(Long.class), any(Long.class));

        Assertions.assertDoesNotThrow(() -> itemService.addItemToCart(cart.getId(), item.getId(), 5L));

        Map<String, String> attrMap = new HashMap<>();
        Assertions.assertDoesNotThrow(() -> itemService.addItemToCart(cart.getId(), attrMap, item.getId(), 5L));
        Assertions.assertEquals(1, attrMap.size());
        Assertions.assertNotNull(attrMap.get(INFO_MESSAGE_ATTRIBUTE_NAME));
        attrMap.clear();

        Assertions.assertDoesNotThrow(() -> itemService.addItemToCart(cart.getId(), attrMap, item.getId(), -1L));
        Assertions.assertEquals(1, attrMap.size());
        Assertions.assertNotNull(attrMap.get(ERROR_ATTRIBUTE_NAME));
        attrMap.clear();
    }

    @Test
    void addItemToCardSecondTest() {
        Item item = getNormalItem();
        Cart cart = new Cart();
        cart.setId(1L);
        item.getChanges().clear();

        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        BDDMockito.given(itemRepository.findById(2L)).willReturn(Optional.empty());
        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(item.getId())).willReturn(item.getQuantity());

        Map<String, String> attrMap = new HashMap<>();

        Assertions.assertDoesNotThrow(() -> itemService.addItemToCart(cart.getId(), attrMap, item.getId(), 2L));
        Assertions.assertEquals(1, attrMap.size());
        Assertions.assertNotNull(attrMap.get(ERROR_ATTRIBUTE_NAME));
        attrMap.clear();

        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addItemToCart(cart.getId(), attrMap, 2L, 1L));
        Assertions.assertTrue(attrMap.isEmpty());

        Assertions.assertThrows(EntityUpdateException.class,
                () -> itemService.addItemToCart(cart.getId(), item.getId(), -1L));
        Assertions.assertThrows(EntityUpdateException.class,
                () -> itemService.addItemToCart(cart.getId(), item.getId(), 2L));

        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addItemToCart(cart.getId(), 2L, 1L));
    }

    @Test
    void attachPriceToItemAndGetItem() {
        Item item = getNormalItem();
        Price price = item.getPrice();

        Price newPrice = new Price();
        newPrice.setId(2L);
        newPrice.setStartDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(25)));
        newPrice.setEndDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(45)));
        newPrice.setPrice(new BigDecimal("102.50"));

        BDDMockito.given(priceService.getPriceById(price.getId())).willReturn(price);
        BDDMockito.given(priceService.getPriceById(newPrice.getId())).willReturn(newPrice);
        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        when(itemRepository.saveAndFlush(any(Item.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertThrows(EntityUpdateException.class, () -> itemService.attachPriceToItemAndGetItem(item.getId(), price.getId()));
        Assertions.assertDoesNotThrow(() -> itemService.attachPriceToItemAndGetItem(item.getId(), newPrice.getId()));
        Assertions.assertEquals(2, item.getPrices().size());

    }

    @Test
    void getItemQuantityInStock() {
        Item item = getNormalItem();

        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(item.getId())).willReturn(item.getQuantity());
        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(2L)).willReturn(0L);

        Assertions.assertEquals(item.getQuantity(), itemService.getItemQuantityInStock(item.getId()));
        Assertions.assertEquals(0, itemService.getItemQuantityInStock(2L));
    }

    @Test
    void findAll() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.findAll()).willReturn(List.of(item));
        Collection<ItemDto> itemDtos = itemService.findAll();
        for (ItemDto itemDto : itemDtos) {
            Assertions.assertNotNull(itemDto.getId());
            Assertions.assertNotNull(itemDto.getDescription());
            Assertions.assertNotNull(itemDto.getName());
            Assertions.assertNotNull(itemDto.getPrice());
            Assertions.assertNotNull(itemDto.getPrice().getItemId());
            Assertions.assertNotNull(itemDto.getPrice().getPrice());
            Assertions.assertNotNull(itemDto.getPrice().getStartDate());
            Assertions.assertNotNull(itemDto.getPrice().getEndDate());
            Assertions.assertNotNull(itemDto.getPrice().getItemName());
        }
    }

    @Test
    void deleteItemById() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(any(Item.class));
        doNothing().when(priceService).removeAllPricesFromItem(any(Item.class));

        Assertions.assertDoesNotThrow(() -> itemService.deleteItemById(item.getId()));

        ArgumentCaptor<Item> itemArgumentCaptor = ArgumentCaptor.forClass(Item.class);

        Mockito.verify(itemRepository).delete(itemArgumentCaptor.capture());
        Item captured = itemArgumentCaptor.getValue();
        Assertions.assertEquals(item.getId(), captured.getId());

        item.setOrders(List.of(new OrderItem()));
        Assertions.assertThrows(EntityUpdateException.class, () -> itemService.deleteItemById(item.getId()));

    }

    @Test
    void getPageSizeProp() {
        Assertions.assertEquals(PAGE_SIZE_PROP, itemService.getPageSizeProp());
    }

    @Test
    void findAllIdAndName() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.findAll()).willReturn(List.of(item));

        Assertions.assertDoesNotThrow(() -> {
            Collection<ItemDto> allIdAndName = itemService.findAllIdAndName();
            Assertions.assertNotEquals(0L, allIdAndName.size());
            for(ItemDto idAndNameDto : allIdAndName) {
                Assertions.assertNotNull(idAndNameDto.getId());
                Assertions.assertNotNull(idAndNameDto.getName());
            }
        });

    }

    @Test
    void getItemImages() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.findById(item.getId())).willReturn(Optional.of(item));

        Assertions.assertDoesNotThrow(() -> {
            Collection<ImageDto> itemImages = itemService.getItemImages(item.getId());
            Assertions.assertEquals(0, itemImages.size());
        });

        Image image = new Image();
        image.setId(1L);
        image.setItem(item);
        image.setImagePath("/images/23426a7d7c8f8d67c7d8f7");
        item.getImages().add(image);

        Assertions.assertDoesNotThrow(() -> {
            Collection<ImageDto> itemImages = itemService.getItemImages(item.getId());
            Assertions.assertEquals(1, itemImages.size());
            for (ImageDto imageDto : itemImages) {
                Assertions.assertNotNull(imageDto.getItemId());
                Assertions.assertNotEquals(0L, imageDto.getImageId());
                Assertions.assertNotNull(imageDto.getItemName());
                Assertions.assertNotNull(imageDto.getImagePath());
            }
        });

    }

    @Test
    void addItemQuantity() {
        Item item = getNormalItem();
        BDDMockito.given(itemRepository.getItemByIdForUpdate(item.getId())).willReturn(Optional.of(item));
        BDDMockito.given(itemQuantityChangeRepository.getItemQuantity(item.getId())).willReturn(item.getQuantity());
        when(itemQuantityChangeRepository.saveAndFlush(any(ItemQuantityChange.class))).then(AdditionalAnswers.returnsFirstArg());
        when(itemRepository.save(any(Item.class))).then(AdditionalAnswers.returnsFirstArg());

        Assertions.assertDoesNotThrow(() -> {
            ItemDto itemDto = itemService.addItemQuantity(item.getId(), 250L, 1L);
            Assertions.assertTrue(itemDto.isInStock());

        });

    }

}
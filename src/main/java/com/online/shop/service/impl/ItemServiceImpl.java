package com.online.shop.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.shop.config.AppConfig;
import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ImageDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.ItemExportDto;
import com.online.shop.entity.Item;
import com.online.shop.entity.ItemQuantityChange;
import com.online.shop.entity.Order;
import com.online.shop.entity.Price;
import com.online.shop.repository.ItemQuantityChangeRepository;
import com.online.shop.repository.ItemRepository;
import com.online.shop.repository.data.ItemQuantityDiffOnPeriodView;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.ExportException;
import com.online.shop.service.exception.ItemCreationException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.InputResource;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemServiceImpl extends BaseService implements ItemService {

    public static final String ITEMS_ATTRIBUTE_NAME = "items";
    public static final String SINGLE_ITEM_ATTRIBUTE_NAME = "item";
    public static final String ITEM_PAGE_ATTRIBUTE_NAME = "itemPage";

    private final ItemRepository itemRepository;
    private final ItemQuantityChangeRepository itemQuantityChangeRepository;
    private final PriceServiceImpl priceService;
    private final CartServiceImpl cartService;
    private final ItemTypeServiceImpl itemTypeService;
    private final TransactionHandler transactionHandler;
    private final PaginationConfig paginationConfig;
    private final AppConfig appConfig;


    private final ObjectMapper mapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, Validator validator, PriceServiceImpl priceService, CartServiceImpl cartService, ItemTypeServiceImpl itemTypeService, ItemQuantityChangeRepository itemQuantityChangeRepository, TransactionHandler transactionHandler, PaginationConfig paginationConfig, AppConfig appConfig) {
        super(validator, LoggerFactory.getLogger(ItemServiceImpl.class));
        this.itemRepository = itemRepository;
        this.priceService = priceService;
        this.cartService = cartService;
        this.itemTypeService = itemTypeService;
        this.itemQuantityChangeRepository = itemQuantityChangeRepository;
        this.transactionHandler = transactionHandler;
        this.paginationConfig = paginationConfig;
        this.appConfig = appConfig;

        this.mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setTimeZone(TimeZone.getTimeZone(appConfig.getAppTimezone()));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setType(itemTypeService.getItemTypeEntityById(itemDto.getItemType().getItemTypeId()));
        return ItemDto.getItemDto(createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long quantity) {
        Item item = getItemById(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setType(itemTypeService.getItemTypeEntityById(itemDto.getItemType().getItemTypeId()));
        long itemQuantityInStock = getItemQuantityInStock(item.getId());
        // case when we do not need to update quantity
        if(itemQuantityInStock == quantity) {
            ItemDto itemResponse = ItemDto.getItemDto(updateItem(item));
            itemResponse.setInStock(itemQuantityInStock > 0);
            return itemResponse;
        }
        updateItem(item);
        // its operation "set" quantity, so we need to remove current quantity value and add(set) new one
        // @Transactional with required new transaction propagation does not work if calling from class method
        return transactionHandler.runInNewTransaction(() -> addItemQuantity(itemDto.getId(), quantity - itemQuantityInStock));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ItemDto addItemQuantity(Long itemId, long quantityToAdd) {
        Item item = getItemByIdForUpdate(itemId);
        ItemQuantityChange change = updateItemQuantity(item, quantityToAdd);
        change.setChangeDate(new Date());
        change = itemQuantityChangeRepository.saveAndFlush(change);
        item.getChanges().add(change);
        return ItemDto.getItemDto(updateItem(item));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ItemDto addItemQuantity(Long itemId, long quantityToAdd, long orderId) {
        Item item = getItemByIdForUpdate(itemId);
        Order order = new Order();
        order.setId(orderId);
        Date currentDate = new Date();
        ItemQuantityChange change = updateItemQuantity(item, quantityToAdd);
        change.setOrder(order);
        change.setChangeDate(currentDate);
        change = itemQuantityChangeRepository.saveAndFlush(change);
        item.getChanges().add(change);
        return ItemDto.getItemDto(updateItem(item));
    }

    @Override
    public ItemDto getById(Long id) {
        ItemDto itemDto = ItemDto.getItemDto(getItemById(id));
        itemDto.setInStock(getItemQuantityInStock(id) > 0);
        return itemDto;
    }

    @Override
    public void addItemToCart(Long cartId, Map<String, String> attributeMap, Long itemId, long quantity) {
        if (quantity < 1) {
            attributeMap.put(ERROR_ATTRIBUTE_NAME, "Please provide quantity greater than zero.");
        } else {
            Item item = getItemById(itemId);
            if (getItemQuantityInStock(itemId) < 1) {
                attributeMap.put(ERROR_ATTRIBUTE_NAME, "Item '" + item.getName() + "' is out of stock.");
            } else {
                cartService.addItemToCart(item, cartId, quantity);
                attributeMap.put(INFO_MESSAGE_ATTRIBUTE_NAME, "Successfully added " + quantity + " '" + item.getName() + "' to your cart.");
            }
        }
    }

    @Override
    public void addItemToCart(Long cartId, Long itemId, long quantity) {
        if (quantity < 1) {
            throw new EntityUpdateException("Cannot add item, please provide quantity greater than zero.");
        } else {
            Item item = getItemById(itemId);
            if (getItemQuantityInStock(itemId) < 1) {
                throw new EntityUpdateException("Item '" + item.getName() + "' is out of stock.");
            } else {
                cartService.addItemToCart(item, cartId, quantity);
            }
        }
    }

    @Override
    public Pagination<ItemDto> findPaginatedByCategory(Long categoryId, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.getItemsByCategoryId(categoryId,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findPaginatedByItemType(Long itemTypeId, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.getItemsByItemTypeId(itemTypeId,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findPaginatedByCategoryAndItemType(Long categoryId, Long itemTypeId, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.getItemsByCategoryIdAndItemTypeId(categoryId, itemTypeId,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findPaginatedByCategoryAndItemTypeAndPropValues(Long categoryId,
                                                                               Long itemTypeId,
                                                                               List<Long> propValueIds,
                                                                               int pageSize,
                                                                               int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.getItemsByCategoryIdAndItemTypeIdAndPropertyValIds(categoryId, itemTypeId, propValueIds,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findPaginatedByCategoryAndItemTypeAndPropValuesStrict(Long itemTypeId,
                                                                                     List<Long> propValueIds,
                                                                                     int pageSize,
                                                                                     int currentPage) {
        List<Long> itemIds = itemRepository.getItemIdsByPropertyValueIdsStrict(
                propValueIds,
                (long) propValueIds.size(),
                itemTypeId
        );
        PaginationInfo paginationInfo =
                getPaginationInfo(itemIds.size(), pageSize, currentPage, getPageSizeProp());
        Collection<Long> itemIdsPage = findNPageInCollection(currentPage, pageSize, itemIds);
        int totalPages = getTotalPages(pageSize, itemIds.size());
        return new Pagination<>(
                new CustomPage<>(
                        itemIdsPage.stream().map(this::getItemById).map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemIdsPage.size(),
                        currentPage,
                        totalPages
                ),
                getPageNumbers(totalPages, paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findByName(String name, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.findByName(name,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findByNameContaining(String name, int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.findByNameContaining(name,
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Pagination<ItemDto> findPaginated(int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(itemRepository.count(), pageSize, currentPage, getPageSizeProp());
        Page<Item> itemPage = itemRepository.findAll(
                    PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
            );
        return new Pagination<>(
                new CustomPage<>(
                        itemPage.getContent().stream().map(ItemDto::getItemDto).collect(Collectors.toList()),
                        itemPage.getSize(),
                        itemPage.getNumber(),
                        itemPage.getTotalPages()
                ),
                getPageNumbers(itemPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public ItemDto attachPriceToItemAndGetItem(Long id, Long priceId) {
        return ItemDto.getItemDto(attachPriceToItemEntityAndGetItem(id, priceId));
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    @Override
    public long getItemQuantityInStock(Long itemId) {
        return Optional.ofNullable(itemQuantityChangeRepository.getItemQuantity(itemId)).orElse(0L);
    }

    @Override
    public long getItemCountByCategoryId(Long categoryId) {
        return itemRepository.getItemsCountByCategoryId(categoryId);
    }

    @Override
    public long getItemCountByItemTypeId(Long itemTypeId) {
        return itemRepository.getItemsCountByItemTypeId(itemTypeId);
    }

    @Override
    public Collection<ItemDto> findAllIdAndName() {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> itemDtos = new ArrayList<>(items.size());
        for(Item item : items) {
            ItemDto result = new ItemDto();
            result.setId(item.getId());
            result.setName(item.getName());
            itemDtos.add(result);
        }
        return itemDtos;
    }

    @Override
    public Collection<ImageDto> getItemImages(Long itemId) {
        Item item = getItemById(itemId);
        return item.getImages()
                .stream()
                .map(ImageDto::getImageDto)
                .collect(Collectors.toList());
    }

    @Override
    public InputResource getAllItemsAsJsonResource() {
        Date currentDate = new Date();
        Collection<ItemExportDto> itemDtos = itemRepository.findAll()
                .stream()
                .map(i -> ItemExportDto.getItemExportDto(i, currentDate))
                .collect(Collectors.toList());
        try {
            setNeededFieldsToNull(itemDtos);
            byte[] bytes = mapper.writeValueAsBytes(itemDtos);
            return new InputResource(
                    new InputStreamResource(new ByteArrayInputStream(bytes)),
                    bytes.length,
                    "items_export.json"
            );
        } catch (Exception ex) {
            logger.error("Exception during items export. Error: " + ex.getMessage());
            throw new ExportException(ex.getMessage());
        }
    }

    @Override
    public Collection<ItemDto> getTopItems(Long limit) {
        Collection<Item> top3ByOrderByOrderItemsSizeDesc = itemRepository.findTopByOrders(limit);
        return ItemDto.getItemDtoCollection(top3ByOrderByOrderItemsSizeDesc);
    }

    public ItemDto addItemQuantityWithoutLock(Long itemId, long quantityToAdd) {
        Item item = getItemById(itemId);
        ItemQuantityChange change = updateItemQuantity(item, quantityToAdd);
        change.setChangeDate(new Date());
        change = itemQuantityChangeRepository.saveAndFlush(change);
        item.getChanges().add(change);
        return ItemDto.getItemDto(updateItem(item));
    }

    private ItemQuantityChange updateItemQuantity(Item item, long quantityToAdd) {
        long itemQuantityInStock = getItemQuantityInStock(item.getId());
        if(itemQuantityInStock + quantityToAdd < 0) {
            throw new EntityUpdateException("Item '" + item.getName() + "' is not in stock.");
        }
        logger.info("Adding quantity change " + quantityToAdd + " to item " + item.getId());
        ItemQuantityChange change = new ItemQuantityChange();
        change.setChange(quantityToAdd);
        change.setChangeDate(new Date());
        Item tempItem = new Item();
        tempItem.setId(item.getId());
        change.setItem(tempItem);
        return change;
    }

    // use only for export of items,
    // unnecessary data for export is set to null and will not be present in result file
    // so when json file will be imported to another database,
    // it will create new entities and will not try to find them by id and throw exceptions
    private void setNeededFieldsToNull(Collection<ItemExportDto> itemDtos) {
        itemDtos.forEach(item -> {
            item.setId(null);
            item.setImages(null);
            item.setInStock(null);
            item.setArticle(null);
            if(item.getPriceHistory() != null) {
                item.getPriceHistory().forEach(price -> {
                    price.setId(null);
                    price.setItemId(null);
                    price.setItemName(null);
                });
            }
            if(item.getPrice() != null) {
                item.getPrice().setId(null);
                item.getPrice().setItemId(null);
                item.getPrice().setItemName(null);
            }
            if(item.getItemType() != null) {
                item.getItemType().setItemTypeId(null);
                item.getItemType().setProperties(null);
                if(item.getItemType().getCategory() != null) {
                    item.getItemType().getCategory().setId(null);
                }
            }
            item.getProperties().forEach(prop -> {
                prop.setPropNameId(null);
                prop.setPropValueId(null);
            });
        });
    }

    private <T> Collection<T> findNPageInCollection(int page, int pageSize, List<T> collection) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = startIndex + pageSize;
        if(startIndex < collection.size()) {
            if(endIndex > collection.size()) {
                endIndex = collection.size();
            }
            Collection<T> result = new ArrayList<>(endIndex - startIndex);
            for(int i = startIndex; i < endIndex; i++) {
                result.add(collection.get(i));
            }
            return result;
        }
        return new ArrayList<>();
    }

    private int getTotalPages(int pageSize, int totalItems) {
        if(pageSize <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal to 0");
        }
        int totalPages = totalItems / pageSize;
        totalPages += (totalItems % pageSize > 0 ? 1 : 0);
        return totalPages;
    }

    protected Item createItem(Item item) {
        Set<ConstraintViolation<Item>> violations = getViolations(item);
        if (violations.isEmpty()) {
            return itemRepository.save(item);
        }
        throw new ItemCreationException(getErrorMessagesTotal(violations));
    }

    public Item updateItem(Item item) {
        Set<ConstraintViolation<Item>> violations = getViolations(item);
        if (violations.isEmpty()) {
            return itemRepository.save(item);
        }
        throw new EntityUpdateException(getErrorMessagesTotal(violations));
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Cannot find item with id '" + id + "'.")
        );
    }

    public Item getItemByIdForUpdate(Long id) {
        return itemRepository.getItemByIdForUpdate(id).orElseThrow(
                () -> new NotFoundException("Cannot find item with id '" + id + "'.")
        );
    }

    public Collection<ItemDto> findAll() {
        return itemRepository.findAll().stream().map(ItemDto::getItemDto).collect(Collectors.toList());
    }

    @Override
    public Collection<ItemQuantityDiffOnPeriodView> getAllWithChangeOnPeriod(Date startDate, Date endDate) {
        return itemQuantityChangeRepository.getItemQuantityChangeOnPeriod(startDate, endDate);
    }

    public void deleteItemById(Long itemId) {
        Item itemToDelete = getItemById(itemId);
        priceService.removeAllPricesFromItem(itemToDelete);
        if (itemToDelete.getOrders().size() != 0) {
            throw new EntityUpdateException("Cannot delete items that are already ordered.");
        }
        itemRepository.delete(itemToDelete);
    }

    protected Item attachPriceToItemEntityAndGetItem(Long itemId, Long priceId) {
        Item item = getItemById(itemId);
        Price price = priceService.getPriceById(priceId);
        for (Price itemPrice : item.getPrices()) {
            if (!PriceServiceImpl.areCompatible(price, itemPrice)) {
                throw new EntityUpdateException("Current price and item prices are not compatible.");
            }
        }
        item.getPrices().add(price);
        price.setItem(item);
        return itemRepository.saveAndFlush(item);
    }

}

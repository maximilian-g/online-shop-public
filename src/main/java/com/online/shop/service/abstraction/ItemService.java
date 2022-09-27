package com.online.shop.service.abstraction;

import com.online.shop.dto.ImageDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.repository.data.ItemQuantityDiffOnPeriodView;
import com.online.shop.service.util.InputResource;
import com.online.shop.service.util.Pagination;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ItemService {

    void addItemToCart(Long cartId, Map<String, String> resultAttributeMap, Long itemId, long quantity);

    void addItemToCart(Long cartId, Long itemId, long quantity);

    Pagination<ItemDto> findPaginated(int pageSize, int currentPage);

    Pagination<ItemDto> findPaginatedByCategory(Long categoryId, int pageSize, int currentPage);

    Pagination<ItemDto> findPaginatedByItemType(Long itemTypeId, int pageSize, int currentPage);

    Pagination<ItemDto> findPaginatedByCategoryAndItemType(Long categoryId, Long itemTypeId, int pageSize, int currentPage);

    Pagination<ItemDto> findPaginatedByCategoryAndItemTypeAndPropValues(Long categoryId,
                                                                        Long itemTypeId,
                                                                        List<Long> propValueIds,
                                                                        int pageSize,
                                                                        int currentPage);

    Pagination<ItemDto> findPaginatedByCategoryAndItemTypeAndPropValuesStrict(Long itemTypeId,
                                                                              List<Long> propValueIds,
                                                                              int pageSize,
                                                                              int currentPage);

    Pagination<ItemDto> findByName(String name,
                                   int pageSize,
                                   int currentPage);

    Pagination<ItemDto> findByNameContaining(String name,
                                             int pageSize,
                                             int currentPage);

    Collection<ItemDto> findAll();

    Collection<ItemQuantityDiffOnPeriodView> getAllWithChangeOnPeriod(Date startDate, Date endDate);

    Collection<ItemDto> findAllIdAndName();

    int getPageSizeProp();

    long getItemQuantityInStock(Long itemId);

    long getItemCountByCategoryId(Long categoryId);

    long getItemCountByItemTypeId(Long itemTypeId);

    ItemDto createItem(ItemDto item);

    ItemDto updateItem(ItemDto item, long quantity);

    ItemDto addItemQuantity(Long itemId, long quantityToAdd);

    ItemDto addItemQuantity(Long itemId, long quantityToAdd, long orderId);

    ItemDto getById(Long id);

    ItemDto attachPriceToItemAndGetItem(Long id, Long priceId);

    void deleteItemById(Long itemId);

    Collection<ImageDto> getItemImages(Long itemId);

    InputResource getAllItemsAsJsonResource();

    Collection<ItemDto> getTopItems(Long limit);
}

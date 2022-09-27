package com.online.shop.service.abstraction;

import com.online.shop.dto.ItemTypeDto;
import com.online.shop.dto.min.ItemTypeDtoMinimized;
import com.online.shop.service.util.Pagination;

import java.util.Collection;
import java.util.List;

public interface ItemTypeService {

    ItemTypeDto createItemType(String name, Long categoryId);

    ItemTypeDto updateItemType(Long itemTypeId, String newName, Long categoryId);

    Pagination<ItemTypeDto> findPaginated(int pageSize, int currentPage);

    ItemTypeDto getById(Long itemTypeId);

    void deleteItemType(Long itemTypeId);

    List<ItemTypeDto> getAllItemTypes();

    int getPageSizeProp();

    Collection<ItemTypeDtoMinimized> findAllItemTypesMin();

    boolean existsById(Long itemTypeId);

    boolean existsByName(String name);

    ItemTypeDto getByName(String name);
}

package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ItemTypeDto;
import com.online.shop.dto.min.ItemTypeDtoMinimized;
import com.online.shop.entity.Category;
import com.online.shop.entity.ItemType;
import com.online.shop.repository.ItemTypeRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.ItemTypeService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemTypeServiceImpl extends BaseService implements ItemTypeService {

    public static final String ITEM_TYPES_ATTRIBUTE_NAME = "itemTypes";
    public static final String ITEM_TYPES_PAGE_ATTRIBUTE_NAME = "itemTypePage";
    public static final String SINGLE_ITEM_TYPE_ATTRIBUTE_NAME = "itemType";
    public static final Long DEFAULT_ITEM_TYPE_ID = -1L;

    private final ItemTypeRepository itemTypeRepository;
    private final CategoryServiceImpl categoryService;
    private final PaginationConfig paginationConfig;

    @Autowired
    public ItemTypeServiceImpl(ItemTypeRepository itemTypeRepository,
                               Validator validator,
                               CategoryServiceImpl categoryService,
                               PaginationConfig paginationConfig) {
        super(validator, LoggerFactory.getLogger(ItemTypeServiceImpl.class));
        this.itemTypeRepository = itemTypeRepository;
        this.categoryService = categoryService;
        this.paginationConfig = paginationConfig;
    }

    @Override
    public ItemTypeDto createItemType(String name, Long categoryId) {
        Category categoryById = categoryService.getCategoryById(categoryId);
        ItemType type = new ItemType();
        type.setName(name);
        type.setCategory(categoryById);
        logger.info("Creating item type " + name);
        return ItemTypeDto.getItemTypeDto(saveIfValidOrThrow(type, itemTypeRepository));
    }

    @Override
    public ItemTypeDto updateItemType(Long itemTypeId, String newName, Long categoryId) {
        ItemType itemTypeById = getItemTypeEntityById(itemTypeId);
        itemTypeById.setName(newName);
        logger.info("Updating item type " + itemTypeId);
        if(!itemTypeById.getCategory().getId().equals(categoryId)) {
            logger.info("Updating category " + categoryId);
            itemTypeById.setCategory(categoryService.getCategoryById(categoryId));
        }
        return ItemTypeDto.getItemTypeDto(saveIfValidOrThrow(itemTypeById, itemTypeRepository));
    }

    @Override
    public Pagination<ItemTypeDto> findPaginated(int pageSize, int currentPage) {
        logger.debug("Getting page " + currentPage + ", size " + pageSize);
        PaginationInfo paginationInfo =
                getPaginationInfo(itemTypeRepository.count(), pageSize, currentPage, paginationConfig.getPageSizeProp());
        Page<ItemType> itemTypePage = itemTypeRepository.findAll(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        itemTypePage.getContent().stream().map(ItemTypeDto::getItemTypeDto).collect(Collectors.toList()),
                        itemTypePage.getSize(),
                        itemTypePage.getNumber(),
                        itemTypePage.getTotalPages()
                ),
                getPageNumbers(itemTypePage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public ItemTypeDto getById(Long itemTypeId) {
        return ItemTypeDto.getItemTypeDto(getItemTypeEntityById(itemTypeId));
    }

    @Override
    public void deleteItemType(Long itemTypeId) {
        ItemType itemType = getItemTypeEntityById(itemTypeId);
        if(!itemType.getProperties().isEmpty()) {
            throw new EntityUpdateException("Cannot delete item type that has associated properties.");
        }
        logger.info("Deleting item type #" + itemTypeId);
        itemTypeRepository.deleteById(itemTypeId);
    }

    @Override
    public List<ItemTypeDto> getAllItemTypes() {
        return ItemTypeDto.getItemTypeDtoList(itemTypeRepository.findAll());
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    @Override
    public Collection<ItemTypeDtoMinimized> findAllItemTypesMin() {
        return itemTypeRepository.findAll()
                .stream()
                .map(ItemTypeDtoMinimized::getMinimizedDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long itemTypeId) {
        return itemTypeRepository.existsById(itemTypeId);
    }

    @Override
    public boolean existsByName(String name) {
        return itemTypeRepository.existsByName(name);
    }

    @Override
    public ItemTypeDto getByName(String name) {
        return ItemTypeDto.getItemTypeDto(getItemTypeEntityByName(name));
    }

    public ItemType getItemTypeEntityById(Long itemTypeId) {
        return itemTypeRepository.findById(itemTypeId)
                .orElseThrow(() -> new NotFoundException("Cannot find Item Type with id '" + itemTypeId + "'"));
    }

    public ItemType getItemTypeEntityByName(String name) {
        return itemTypeRepository.findItemTypeByName(name)
                .orElseThrow(() -> new NotFoundException("Cannot find Item Type with name '" + name + "'"));
    }

}

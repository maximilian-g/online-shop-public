package com.online.shop.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.online.shop.dto.CategoryDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.ItemExportDto;
import com.online.shop.dto.ItemPropertyDto;
import com.online.shop.dto.ItemTypeDto;
import com.online.shop.dto.PriceDto;
import com.online.shop.dto.PropertyDto;
import com.online.shop.dto.PropertyValueDto;
import com.online.shop.dto.min.CategoryDtoMinimized;
import com.online.shop.entity.Price;
import com.online.shop.service.abstraction.LoggableService;
import com.online.shop.service.exception.BulkUploadException;
import com.online.shop.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Items are imported as list of {@link ItemDto} 's.
 * Rules:
 * Item must have price, item type and category. {@link ItemTypeDto} {@link CategoryDto}
 * Item type, category are searched by id firstly.
 * If there are no id or entity was not found, new entity is created.
 * <p>
 * Properties of item are searched by name ONLY. {@link ItemPropertyDto}
 * That means that if current ItemTypeDto has properties,
 * we will iterate through them and will try to find match in property name and value.
 * If there is no match for desired property value, then there are 3 cases:
 * 1 - property not found at all - property and property value are created
 * 2 - property found, property value not found - property value is created
 * 3 - property and property value are found - found property value will be used
 * <p>
 * Every element of input array is processed individually.
 * That means if file is valid and was parsed correctly,
 * every single item upload will be executed in new transaction and if error occurred,
 * exception will be thrown and written to output array of logs.
 * <p>
 * There is API endpoint for handling files - /api/v1/items/bulk_upload,
 * returns list of logs(errors or message "Item ... was successfully loaded")
 */
@Service
@Transactional
public class ItemBulkUploadService extends LoggableService {

    private final ItemServiceImpl itemService;
    private final PriceServiceImpl priceService;
    private final ItemTypeServiceImpl itemTypeService;
    private final CategoryServiceImpl categoryService;
    private final PropertyServiceImpl propertyService;
    private final TransactionHandler transactionHandler;

    private final ObjectMapper objectMapper;

    @Autowired
    public ItemBulkUploadService(ItemServiceImpl itemService,
                                 PriceServiceImpl priceService,
                                 ItemTypeServiceImpl itemTypeService,
                                 CategoryServiceImpl categoryService,
                                 PropertyServiceImpl propertyService,
                                 TransactionHandler transactionHandler) {
        super(LoggerFactory.getLogger(ItemBulkUploadService.class));
        this.itemService = itemService;
        this.priceService = priceService;
        this.itemTypeService = itemTypeService;
        this.categoryService = categoryService;
        this.propertyService = propertyService;
        this.transactionHandler = transactionHandler;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<String> uploadItemsFile(MultipartFile file) {
        // convert to List of items
        List<ItemExportDto> itemsToUpload = convertToItems(file);
        return uploadItems(itemsToUpload);
    }

    public ItemDto uploadItem(ItemExportDto item) {
        CategoryDto categoryDto = getCategoryDto(item);
        ItemTypeDto itemTypeDto = getItemTypeDto(item, categoryDto);

        item.getPriceHistory().forEach(this::correctPriceDate);

        List<PriceDto> allPrices = item.getPriceHistory()
                .stream()
                .map(this::getPrice).collect(Collectors.toList());

        ItemDto itemDto = new ItemDto();
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setItemType(itemTypeDto);
        itemDto = itemService.createItem(itemDto);
        itemDto = itemService.addItemQuantityWithoutLock(itemDto.getId(), item.getQuantityOnExportMoment());
        int i = 0;
        for(PriceDto otherPrice : allPrices) {
            logger.debug("Loading price " + i++);
            itemDto = itemService.attachPriceToItemAndGetItem(itemDto.getId(), otherPrice.getId());
        }
        logger.debug("Loaded " + i + " prices");

        List<ItemPropertyDto> properties = item.getProperties();

        for(ItemPropertyDto dto : properties) {
            // properties are found only by names
            boolean propertyFound = false;
            boolean propertyValueFound = false;
            PropertyDto propertyDtoToFind = null;
            PropertyValueDto propertyValueDtoToFind = null;
            for(PropertyDto propertyDto : itemTypeDto.getProperties()) {
                if(propertyDto.getName().equalsIgnoreCase(dto.getName())) {
                    propertyFound = true;
                    propertyDtoToFind = propertyDto;
                    for(PropertyValueDto propertyValueDto : propertyDto.getPropertyValues()) {
                        if (propertyValueDto.getValue().equalsIgnoreCase(dto.getValue())) {
                            propertyValueFound = true;
                            propertyValueDtoToFind = propertyValueDto;
                            break;
                        }
                    }
                }
            }
            Long propertyValId;
            // property not found at all case
            if(!propertyFound) {
                PropertyDto propertyDto = new PropertyDto();
                propertyDto.setItemTypeId(itemTypeDto.getItemTypeId());
                propertyDto.setName(dto.getName());
                PropertyDto property = propertyService.createProperty(propertyDto);
                PropertyValueDto propertyVal = propertyService.createPropertyVal(dto.getValue(), property.getId());
                propertyValId = propertyVal.getPropValueId();
            } else if(!propertyValueFound) {
                // property found but value not found
                PropertyValueDto propertyVal = propertyService.createPropertyVal(dto.getValue(),
                        propertyDtoToFind.getId());
                propertyValId = propertyVal.getPropValueId();
            } else {
                // property and value found
                propertyValId = propertyValueDtoToFind.getPropValueId();
            }
            propertyService.assignItemToPropertyVal(propertyValId, itemDto.getId());
        }
        return itemDto;
    }

    // there is case when we receive date with time,
    // in this application price must have no time, date only
    protected void correctPriceDate(PriceDto priceDto) {
        if(priceDto != null) {
            if(priceDto.getStartDate() != null) {
                priceDto.setStartDate(DateUtil.removeTime(priceDto.getStartDate()));
            }
            if(priceDto.getEndDate() != null) {
                priceDto.setEndDate(DateUtil.removeTime(priceDto.getEndDate()));
            }
        }
    }

    protected PriceDto getPrice(PriceDto priceDto) {
        if(priceDto == null ||
                priceDto.getPrice() == null ||
                priceDto.getStartDate() == null) {
            throw new BulkUploadException("Price, start date and amount must be defined.");
        }
        if(priceDto.getId() == null || !priceService.existsById(priceDto.getId())) {
            Price price = new Price();
            price.setPrice(priceDto.getPrice());
            price.setStartDate(DateUtil.removeTime(priceDto.getStartDate()));
            price.setEndDate(priceDto.getEndDate() == null ? null : DateUtil.removeTime(priceDto.getEndDate()));
            return priceService.createPrice(price);
        }
        return priceService.getById(priceDto.getId());
    }

    protected CategoryDto getCategoryDto(ItemDto item) {
        if(item.getItemType() == null ||
                item.getItemType().getCategory() == null ||
                item.getItemType().getCategory().getName() == null) {
            throw new BulkUploadException("Category must be provided within item type.");
        }

        CategoryDtoMinimized category = item.getItemType().getCategory();

        // case when id is not present, category will be found by name or will be created
        if(category.getId() == null) {
            if(categoryService.existsByName(category.getName())) {
                return categoryService.getByName(category.getName());
            }
            return categoryService.createCategory(category.getName());
        }

        // case when id is present, name equality is ignored in this case
        return categoryService.getById(category.getId());
    }

    protected ItemTypeDto getItemTypeDto(ItemDto item, CategoryDto category) {
        if(item.getItemType() == null ||
                item.getItemType().getName() == null) {
            throw new BulkUploadException("Item type must be provided.");
        }
        ItemTypeDto itemTypeDto = new ItemTypeDto();
        itemTypeDto.setItemTypeId(item.getItemType().getItemTypeId());
        itemTypeDto.setName(item.getItemType().getName());

        // firstly id is checked, if there is such category with this id,
        // it will be considered as needed category(if name is not same, it will be ignored)
        if(itemTypeDto.getItemTypeId() == null) {
            if(itemTypeService.existsByName(itemTypeDto.getName())) {
                itemTypeDto = itemTypeService.getByName(itemTypeDto.getName());
                // case when found by name but not attached to given category
                if(!itemTypeDto.getCategory().getId().equals(category.getId())) {
                    return itemTypeService
                            .createItemType(itemTypeDto.getName(), category.getId());
                }
                return itemTypeDto;
            }
            return itemTypeService
                    .createItemType(itemTypeDto.getName(), category.getId());
        }

        // if item type does not exist by id or if item type does not belong to given category
        if(!itemTypeService.existsById(itemTypeDto.getItemTypeId()) ||
                category.getItemTypes()
                        .stream()
                        .noneMatch(type -> type.getId().equals(item.getItemType().getItemTypeId()))) {
            return itemTypeService
                    .createItemType(itemTypeDto.getName(), category.getId());
        }
        // case where item type exists by id and belongs to given category,
        // name is ignored in this case
        return itemTypeService.getById(itemTypeDto.getItemTypeId());
    }

    protected List<ItemExportDto> convertToItems(MultipartFile file) {
        try {
            return Arrays.stream(objectMapper.readValue(file.getBytes(), ItemExportDto[].class))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            logger.error("File '" + file.getOriginalFilename() + "' is corrupted or invalid.");
            throw new BulkUploadException("Could not read file");
        }
    }

    protected List<String> uploadItems(List<ItemExportDto> items) {
        int i = 1;
        List<String> result = new ArrayList<>(items.size());
        for(ItemExportDto item : items) {
            try {
                // for every item new transaction will be created,
                // so we can process exception correctly and continue to process items
                ItemDto itemDto = transactionHandler.runInNewTransaction(() -> uploadItem(item));
                result.add("Item " + i + " successfully uploaded, id #" + itemDto.getId());
                logger.info("Uploaded item #" + itemDto.getId());
            } catch (Exception ex) {
                logger.error("Exception during upload of item " + i);
                result.add("Exception while uploading " + i + "'th item. Error: '" + ex.getMessage() + "'");
            }
            i++;
        }
        return result;
    }

}

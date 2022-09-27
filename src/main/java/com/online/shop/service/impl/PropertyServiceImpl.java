package com.online.shop.service.impl;

import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.PropertyDto;
import com.online.shop.dto.PropertyValueDto;
import com.online.shop.entity.ItemPropertyValueEntity;
import com.online.shop.entity.ItemPropertyValuePrimaryKey;
import com.online.shop.entity.ItemType;
import com.online.shop.entity.Property;
import com.online.shop.entity.PropertyValue;
import com.online.shop.repository.ItemPropertyValueRepository;
import com.online.shop.repository.PropertyRepository;
import com.online.shop.repository.PropertyValueRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.PropertyService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import com.online.shop.service.util.CustomPage;
import com.online.shop.service.util.Pagination;
import com.online.shop.service.util.PaginationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PropertyServiceImpl extends BaseService implements PropertyService {

    public static final String PROPERTIES_PAGE_ATTRIBUTE_NAME = "propertyPage";
    public static final String SINGLE_PROPERTY_ATTRIBUTE_NAME = "property";
    public static final String SINGLE_PROPERTY_VAL_ATTRIBUTE_NAME = "propertyVal";
    public static final String PROPERTIES_MSG_ATTRIBUTE_NAME = "propertiesMessages";
    public static final String PROPERTIES_MSG_STR_ATTRIBUTE_NAME = "propertiesMessagesStr";
    public static final String PROPERTIES_IDS_ATTRIBUTE_NAME = "propertiesIds";

    private final PropertyRepository propertyRepository;
    private final PropertyValueRepository propertyValueRepository;
    private final ItemTypeServiceImpl itemTypeService;
    private final PaginationConfig paginationConfig;
    private final ItemPropertyValueRepository itemPropertyValueRepository;

    protected PropertyServiceImpl(Validator validator,
                                  PropertyRepository propertyRepository,
                                  PropertyValueRepository propertyValueRepository,
                                  ItemTypeServiceImpl itemTypeService,
                                  PaginationConfig paginationConfig, ItemPropertyValueRepository itemPropertyValueRepository) {
        super(validator, LoggerFactory.getLogger(PropertyServiceImpl.class));
        this.propertyRepository = propertyRepository;
        this.propertyValueRepository = propertyValueRepository;
        this.itemTypeService = itemTypeService;
        this.paginationConfig = paginationConfig;
        this.itemPropertyValueRepository = itemPropertyValueRepository;
    }

    @Override
    public Pagination<PropertyDto> findPaginated(int pageSize, int currentPage) {
        logger.debug("Getting page " + currentPage + ", size " + pageSize);
        PaginationInfo paginationInfo =
                getPaginationInfo(propertyRepository.count(), pageSize, currentPage, paginationConfig.getPageSizeProp());
        Page<Property> propertyPage = propertyRepository.findAll(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        propertyPage.getContent().stream().map(PropertyDto::getPropertyDto).collect(Collectors.toList()),
                        propertyPage.getSize(),
                        propertyPage.getNumber(),
                        propertyPage.getTotalPages()
                ),
                getPageNumbers(propertyPage.getTotalPages(), paginationInfo.currentPage));
    }

    @Override
    public Collection<PropertyDto> getAll() {
        return propertyRepository.findAll().stream().map(PropertyDto::getPropertyDto).collect(Collectors.toList());
    }

    @Override
    public PropertyDto getById(Long id) {
        return PropertyDto.getPropertyDto(getPropertyById(id));
    }

    @Override
    public PropertyValueDto getPropValueById(Long id) {
        return PropertyValueDto.getPropertyValueDto(getPropertyValById(id));
    }

    @Override
    public PropertyDto createProperty(PropertyDto propertyDto) {
        Property property = new Property();
        property.setName(propertyDto.getName());
        ItemType itemType = itemTypeService.getItemTypeEntityById(propertyDto.getItemTypeId());
        property.setItemType(itemType);
        logger.info("Creating property '" + property.getName() + "'");
        return PropertyDto.getPropertyDto(saveIfValidOrThrow(property, propertyRepository));
    }

    @Override
    public PropertyValueDto createPropertyVal(String propertyValue, Long propertyId) {
        Property property = getPropertyById(propertyId);
        PropertyValue propertyVal = new PropertyValue();
        propertyVal.setProperty(property);
        propertyVal.setValue(propertyValue);
        logger.info("Creating property value '" + propertyVal.getValue() + "' for property '" + property.getName() + "'");
        return PropertyValueDto.getPropertyValueDto(saveIfValidOrThrow(propertyVal, propertyValueRepository));
    }

    @Override
    public PropertyDto updateProperty(Long id, PropertyDto propertyDto) {
        Property property = getPropertyById(id);
        logger.info("Updating property '" + property.getName() + "'");
        property.setName(propertyDto.getName());
        if(!property.getItemType().getId().equals(propertyDto.getItemTypeId())) {
            ItemType itemType = itemTypeService.getItemTypeEntityById(propertyDto.getItemTypeId());
            logger.info("New item type for property '" + property.getId() + "' is '" + itemType.getName() + "(" + itemType.getId() + ")'");
            property.setItemType(itemType);
        }
        return PropertyDto.getPropertyDto(saveIfValidOrThrow(property, propertyRepository));
    }

    @Override
    public PropertyValueDto updatePropertyVal(Long propValId, String propertyValue, Long propertyId) {
        PropertyValue propertyVal = getPropertyValById(propValId);
        if(!propertyVal.getProperty().getId().equals(propertyId)) {
            Property property = getPropertyById(propertyId);
            propertyVal.setProperty(property);
        }
        propertyVal.setValue(propertyValue);
        logger.info("Updating property value. New value '" + propertyValue + "'.");
        return PropertyValueDto.getPropertyValueDto(saveIfValidOrThrow(propertyVal, propertyValueRepository));
    }

    @Override
    public Collection<ItemDto> getItemsOfPropertyValue(Long propValId) {
        PropertyValue propertyVal = getPropertyValById(propValId);
        return propertyVal.getItemPropertyLink()
                .stream()
                .map(link -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(link.getItem().getId());
                    itemDto.setName(link.getItem().getName());
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getStringRepresentationOf(List<Long> propertyValueIds) {
        return propertyValueIds.stream()
                .map(id -> {
                    PropertyValue propertyValById = getPropertyValById(id);
                    return propertyValById.getProperty().getName() + ": " + propertyValById.getValue();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void assignItemToPropertyVal(Long propValId, Long itemId) {
        PropertyValue propVal = getPropertyValById(propValId);
        ItemPropertyValuePrimaryKey pk = new ItemPropertyValuePrimaryKey();
        pk.setItemId(itemId);
        pk.setPropertyValueId(propValId);
        if(itemPropertyValueRepository.existsById(pk)) {
            throw new EntityUpdateException("Cannot create link from item #" + itemId +
                    " to property value #" + propValId);
        }
        ItemPropertyValueEntity entity = new ItemPropertyValueEntity();
        entity.setId(pk);
        entity.setPropertyValue(propVal);
        entity = itemPropertyValueRepository.saveAndFlush(entity);
        propVal.getItemPropertyLink().add(entity);
        propertyValueRepository.saveAndFlush(propVal);
    }

    @Override
    public void removeItemFromPropertyVal(Long propValId, Long itemId) {
        PropertyValue propVal = getPropertyValById(propValId);
        Optional<ItemPropertyValueEntity> first = propVal.getItemPropertyLink()
                .stream()
                .filter(link -> link.getId().getItemId().equals(itemId))
                .findFirst();
        if(first.isEmpty()) {
            throw new EntityUpdateException("Item #" + itemId + " is not assigned to property value #" + propValId);
        }
        itemPropertyValueRepository.deleteById(first.get().getId());
    }

    @Override
    public void deletePropertyById(Long id) {
        logger.info("Trying to delete property with id #" + id);
        Property property = getPropertyById(id);
        if(!property.getPropertyValues().isEmpty()) {
            throw new EntityUpdateException("Cannot delete property that have property values.");
        }
        propertyRepository.deleteById(property.getId());
    }

    @Override
    public void deletePropertyValueById(Long id) {
        logger.info("Trying to delete property value with id #" + id);
        PropertyValue propertyVal = getPropertyValById(id);
        for(ItemPropertyValueEntity link : propertyVal.getItemPropertyLink()) {
            itemPropertyValueRepository.deleteById(link.getId());
        }
        propertyValueRepository.deleteById(propertyVal.getId());
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property with id '" + id + "' not found."));
    }

    public PropertyValue getPropertyValById(Long id) {
        return propertyValueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Property value with id '" + id + "' not found."));
    }

}

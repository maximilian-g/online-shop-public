package com.online.shop.service.abstraction;

import com.online.shop.dto.ItemDto;
import com.online.shop.dto.PropertyDto;
import com.online.shop.dto.PropertyValueDto;
import com.online.shop.service.util.Pagination;

import java.util.Collection;
import java.util.List;

public interface PropertyService {

    Pagination<PropertyDto> findPaginated(int pageSize, int currentPage);

    Collection<PropertyDto> getAll();

    PropertyDto getById(Long id);

    PropertyValueDto getPropValueById(Long id);

    PropertyDto createProperty(PropertyDto propertyDto);

    PropertyValueDto createPropertyVal(String propertyValue, Long propertyId);

    PropertyDto updateProperty(Long id, PropertyDto propertyDto);

    PropertyValueDto updatePropertyVal(Long propValId, String propertyValue, Long propertyId);

    Collection<ItemDto> getItemsOfPropertyValue(Long propValId);

    List<String> getStringRepresentationOf(List<Long> propertyValueIds);

    void assignItemToPropertyVal(Long propValId, Long itemId);

    void removeItemFromPropertyVal(Long propValId, Long itemId);

    void deletePropertyById(Long id);

    void deletePropertyValueById(Long id);

    int getPageSizeProp();

}

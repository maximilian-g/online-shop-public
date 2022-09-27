package com.online.shop.service.abstraction;

import com.online.shop.dto.AddressDto;
import com.online.shop.service.util.Pagination;

public interface AddressService {

    AddressDto getById(Long id);

    AddressDto createAddress(AddressDto newAddress, Long requestUserId);

    AddressDto createAddress(String newAddress, Long ownerId);

    AddressDto updateAddress(AddressDto editedAddress, Long requestUserId);

    Pagination<AddressDto> findPaginatedAddresses(int pageSize, int currentPage);

    void deleteById(Long id);

    int getPageSizeProp();

}

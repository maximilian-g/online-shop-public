package com.online.shop.service.impl;

import com.online.shop.api.exception.RestException;
import com.online.shop.config.PaginationConfig;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.Address;
import com.online.shop.repository.AddressRepository;
import com.online.shop.service.abstraction.AddressService;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.exception.AddressCreationException;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressServiceImpl extends BaseService implements AddressService {

    private final AddressRepository addressRepository;
    private final PaginationConfig paginationConfig;
    private final UserServiceImpl userService;

    public static final String ADDRESS_PAGE_ATTRIBUTE_NAME = "addressPage";
    public static final String ADDRESSES_ATTRIBUTE_NAME = "addresses";
    public static final String SINGLE_ADDRESS_ATTRIBUTE_NAME = "address";

    @Autowired
    protected AddressServiceImpl(AddressRepository addressRepository, Validator validator, PaginationConfig paginationConfig, UserServiceImpl userService) {
        super(validator, LoggerFactory.getLogger(AddressServiceImpl.class));
        this.addressRepository = addressRepository;
        this.paginationConfig = paginationConfig;
        this.userService = userService;
    }

    @Override
    public AddressDto getById(Long id) {
        return AddressDto.getAddressDto(getAddressById(id));
    }

    @Override
    public AddressDto createAddress(AddressDto newAddress, Long requestUserId) {
        if (newAddress.getOwnerId() != null) {
            UserDto newOwner = userService.getUserDtoById(newAddress.getOwnerId());
            if (newOwner.getId().equals(requestUserId) ||
                    userService.isAdmin(requestUserId)) {
                return AddressDto.getAddressDto(createAddressAssignedToUser(newAddress.getAddress(), newOwner.getId()));
            } else {
                throw new RestException("Invalid request.");
            }
        } else if (userService.isAdmin(requestUserId)) {
            return AddressDto.getAddressDto(createAddress(newAddress.getAddress()));
        }
        throw new AddressCreationException("Cannot create address.");
    }

    @Override
    public AddressDto createAddress(String newAddress, Long ownerId) {
        return AddressDto.getAddressDto(createAddressAssignedToUser(newAddress, ownerId));
    }

    @Override
    public AddressDto updateAddress(AddressDto editedAddress, Long requestUserId) {
        if (editedAddress.getOwnerId() != null) {
            UserDto newOwner = userService.getUserDtoById(editedAddress.getOwnerId());
            if (newOwner.getId().equals(requestUserId)) {
                return AddressDto.getAddressDto(updateAddress(editedAddress.getId(), editedAddress.getAddress()));
            } else if(userService.isAdmin(requestUserId)) {
                return AddressDto.getAddressDto(updateAddress(editedAddress.getId(), editedAddress.getAddress(), newOwner.getId()));
            }
            throw new RestException("Invalid request.");
        } else if (userService.isAdmin(requestUserId)) {
            return AddressDto.getAddressDto(updateAddress(editedAddress.getId(), editedAddress.getAddress()));
        }
        throw new EntityUpdateException("Cannot update this address.");
    }

    @Override
    public Pagination<AddressDto> findPaginatedAddresses(int pageSize, int currentPage) {
        return findPaginated(pageSize, currentPage);
    }

    @Override
    public void deleteById(Long addressId) {
        Address address = getAddressById(addressId);
        if (address.getOrders().size() > 0) {
            throw new EntityUpdateException("Cannot delete address, which is already used in some order(s).");
        }
        addressRepository.deleteById(addressId);
    }

    @Override
    public int getPageSizeProp() {
        return paginationConfig.getPageSizeProp();
    }

    public Address createAddress(String newAddress) {
        Address address = new Address();
        address.setAddress(newAddress);
        Set<ConstraintViolation<Address>> violations = getViolations(address);
        if (violations.isEmpty()) {
            address = addressRepository.save(address);
        } else {
            throw new AddressCreationException(getErrorMessagesTotal(violations));
        }
        return address;
    }

    public Address createAddressAssignedToUser(String newAddress, Long userId) {
        Address address = new Address();
        address.setAddress(newAddress);
        address.setUser(userService.getUserById(userId));
        Set<ConstraintViolation<Address>> violations = getViolations(address);
        if (violations.isEmpty()) {
            address = addressRepository.save(address);
        } else {
            throw new AddressCreationException(getErrorMessagesTotal(violations));
        }
        return address;
    }

    // updates address and reattaches it to user with id 'userId'
    public Address updateAddress(Long addressId, String newAddress, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Cannot find this address."));
        if (!address.getAddress().equals(newAddress) ||
                address.getUser() == null ||
                !address.getUser().getId().equals(userId)) {
            address.setAddress(newAddress);
            Set<ConstraintViolation<Address>> violations = getViolations(address);
            if (violations.isEmpty()) {
                address.setUser(userService.getUserById(userId));
                return addressRepository.save(address);
            } else {
                throw new EntityUpdateException(getErrorMessagesTotal(violations));
            }
        } else {
            throw new EntityUpdateException("Cannot update address '" + address.getAddress() + "'.");
        }
    }

    // updates address
    public Address updateAddress(Long addressId, String newAddress) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Cannot find this address."));
        address.setAddress(newAddress);
        Set<ConstraintViolation<Address>> violations = getViolations(address);
        if (violations.isEmpty()) {
            return addressRepository.save(address);
        } else {
            throw new EntityUpdateException(getErrorMessagesTotal(violations));
        }
    }

    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Cannot find address with id '" + addressId + "'."));
    }

    protected Pagination<AddressDto> findPaginated(int pageSize, int currentPage) {
        PaginationInfo paginationInfo =
                getPaginationInfo(addressRepository.count(), pageSize, currentPage, paginationConfig.getPageSizeProp());
        Page<Address> addressPage = addressRepository.findAll(
                PageRequest.of(paginationInfo.currentPage - 1, paginationInfo.pageSize)
        );
        return new Pagination<>(
                new CustomPage<>(
                        addressPage.getContent().stream().map(AddressDto::getAddressDto).collect(Collectors.toList()),
                        addressPage.getSize(),
                        addressPage.getNumber(),
                        addressPage.getTotalPages()
                ),
                getPageNumbers(addressPage.getTotalPages(), paginationInfo.currentPage));
    }

}

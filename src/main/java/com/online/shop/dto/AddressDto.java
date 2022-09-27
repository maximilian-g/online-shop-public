package com.online.shop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.IdOnly;
import com.online.shop.dto.transfer.New;
import com.online.shop.entity.Address;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

public class AddressDto {

    @NotNull(groups = {Exists.class, IdOnly.class})
    private Long id;
    private Long ownerId;
    @JsonIgnore
    private String ownerUsername;
    @NotNull(groups = {New.class, Exists.class})
    @NotBlank(groups = {New.class, Exists.class})
    @Size(groups = {New.class, Exists.class}, min = 3, max = 255, message = "Address {app.validation.size.msg} but provided ${validatedValue}")
    private String address;

    public AddressDto(Long id, Long ownerId, String address) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerUsername = "";
        this.address = address;
    }

    public static AddressDto getAddressDto(Address address) {
        AddressDto addressDto = new AddressDto(
                address.getId(),
                address.getUser() == null ? null : address.getUser().getId(),
                address.getAddress()
        );
        addressDto.ownerUsername = address.getUser() == null ? null : address.getUser().getUsername();
        return addressDto;
    }

    public static Collection<AddressDto> getAddressDtoCollection(Collection<Address> addresses) {
        Collection<AddressDto> result = new ArrayList<>(addresses.size());
        for(Address address : addresses) {
            result.add(getAddressDto(address));
        }
        return result;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}

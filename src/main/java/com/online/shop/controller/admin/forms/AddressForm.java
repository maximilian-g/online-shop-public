package com.online.shop.controller.admin.forms;

import com.online.shop.entity.Address;
import com.online.shop.entity.User;

public class AddressForm {
    private String address;
    private Long userId;

    public AddressForm() {

    }

    public AddressForm(String address, Long userId) {
        this.address = address;
        this.userId = userId;
    }

    public Address getAddressEntity() {
        Address result = new Address();
        result.setAddress(address);
        User user = new User();
        user.setId(userId);
        result.setUser(user);
        return result;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

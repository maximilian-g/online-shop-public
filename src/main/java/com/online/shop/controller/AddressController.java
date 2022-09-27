package com.online.shop.controller;

import com.online.shop.dto.AddressDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.AddressService;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.AddressServiceImpl.ADDRESSES_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.UserServiceImpl.EMAIL_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.UserServiceImpl.REGISTERED_AT_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "address")
public class AddressController extends BaseController {

    private final AddressService addressService;

    public AddressController(AddressService addressService, UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(AddressController.class));
        this.addressService = addressService;
    }

    @PostMapping
    public String createAddress(@RequestParam String newAddress, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (user != null) {
            UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
            addUsernameAttribute(model, userDto.getUsername());
            AddressDto addressDto = new AddressDto(-1L, userDto.getId(), newAddress);
            addressService.createAddress(addressDto, userDto.getId());
            // updating dto after address was added
            userDto = userService.getUserDtoByUsername(user.getUsername());
            model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Address '" + newAddress + "' was successfully added.");
            model.addAttribute(EMAIL_ATTRIBUTE_NAME, userDto.getEmail());
            model.addAttribute(REGISTERED_AT_ATTRIBUTE_NAME, userDto.getRegisteredAt());
            model.addAttribute(ADDRESSES_ATTRIBUTE_NAME, userDto.getAddresses());
        } else {
            return "login";
        }

        return "account";
    }

    @DeleteMapping(path = "/{id}")
    public String deleteAddress(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        if (user != null) {
            UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
            addUsernameAttribute(model, userDto.getUsername());
            if (userService.userHasAddressWithId(userDto.getId(), id)) {
                addressService.deleteById(id);
                model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Successfully deleted chosen address.");
            } else {
                model.addAttribute(ERROR_ATTRIBUTE_NAME, "Cannot delete this address.");
            }
            model.addAttribute(EMAIL_ATTRIBUTE_NAME, userDto.getEmail());
            model.addAttribute(REGISTERED_AT_ATTRIBUTE_NAME, userDto.getRegisteredAt());
            model.addAttribute(ADDRESSES_ATTRIBUTE_NAME, userDto.getAddresses());
        } else {
            return "login";
        }
        return "account";
    }

}

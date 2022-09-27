package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.controller.admin.forms.AddressForm;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.UserDto;
import com.online.shop.service.abstraction.AddressService;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.impl.AddressServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/admin/address")
public class AdminAddressController extends BaseController {

    private final AddressService addressService;

    protected AdminAddressController(UserServiceImpl userService, AddressService addressService) {
        super(userService, LoggerFactory.getLogger(AdminAddressController.class));
        this.addressService = addressService;
    }

    @GetMapping
    public String getAddress(@RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size,
                             Model model) {
        addAuthAttribute(model);
        addPaginationToModel(model,
                size.orElse(addressService.getPageSizeProp()),
                page.orElse(1));
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        return "adminAddress";
    }

    @GetMapping("/{id}")
    public String getSingleAddress(Model model,
                                   @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(AddressServiceImpl.SINGLE_ADDRESS_ATTRIBUTE_NAME, addressService.getById(id));
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        return "adminAddress";
    }

    @PostMapping
    public String createAddress(AddressForm form, Model model) {
        addAuthAttribute(model);
        if (form.getAddress() != null && form.getUserId() != null) {
            model.addAttribute(AddressServiceImpl.SINGLE_ADDRESS_ATTRIBUTE_NAME,
                    addressService.createAddress(form.getAddress(),
                            form.getUserId()));
            model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
            model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Address was successfully created");
        } else {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Could not create address");
        }
        return "adminAddress";
    }

    @PutMapping("/{id}")
    public String editAddress(@PathVariable Long id, AddressForm form, Model model) {
        addAuthAttribute(model);
        UserDto requestUser = userService.getUserDtoByUsername(getUserFromAuthentication().getUsername());
        model.addAttribute(AddressServiceImpl.SINGLE_ADDRESS_ATTRIBUTE_NAME,
                addressService.updateAddress(new AddressDto(id, requestUser.getId(), form.getAddress()), requestUser.getId()));
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Address was successfully updated");
        return "adminAddress";
    }

    @DeleteMapping("/{id}")
    public String removeAddress(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        addressService.deleteById(id);
        model.addAttribute(UserServiceImpl.USER_ATTRIBUTE_NAME, userService.getAllUsers());
        addPaginationToModel(model, addressService.getPageSizeProp(), 1);
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Address with id '" + id + "' was successfully deleted");
        return "adminAddress";
    }

    private void addPaginationToModel(Model model, int pageSize, int currentPage) {
        Pagination<AddressDto> pagination = addressService.findPaginatedAddresses(
                pageSize, currentPage
        );
        model.addAttribute(
                AddressServiceImpl.ADDRESS_PAGE_ATTRIBUTE_NAME,
                pagination.page
        );
        model.addAttribute(BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
    }

}

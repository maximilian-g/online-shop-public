package com.online.shop.api;

import com.online.shop.api.exception.RestException;
import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.AddressDto;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.AddressService;
import com.online.shop.service.abstraction.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/addresses")
public class AddressRestController extends BaseRestController {

    private final AddressService addressService;
    private final UserService userService;

    public AddressRestController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AddressDto getAddressDtoById(@PathVariable Long id) {
        Long currentUserId = userService.getUserIdByUsername(getCurrentUserUsername());
        if(userService.userHasAddressWithId(currentUserId, id) || userService.isAdmin(currentUserId)) {
            return addressService.getById(id);
        }
        throw getPermissionRestException();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAddress(@Validated(New.class) @RequestBody AddressDto addressDto) {
        Long requestUserId = userService.getUserIdByUsername(getCurrentUserUsername());
        addressDto = addressService.createAddress(addressDto, requestUserId);
        return ResponseEntity.ok(addressDto);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAddress(@Validated(Exists.class) @RequestBody AddressDto addressDto) {
        Long requestUserId = userService.getUserIdByUsername(getCurrentUserUsername());
        addressDto = addressService.updateAddress(addressDto, requestUserId);
        return ResponseEntity.ok(addressDto);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        Long requestUserId = userService.getUserIdByUsername(getCurrentUserUsername());
        if(userService.userHasAddressWithId(requestUserId, id) || userService.isAdmin(requestUserId)) {
            addressService.deleteById(id);
            return ResponseEntity.ok(new ResponseObject(
                    true,
                    HttpStatus.OK.value(),
                    "Successfully deleted address with id '" + id + "'"
            ));
        }
        throw new RestException("Cannot delete this address.");
    }

}

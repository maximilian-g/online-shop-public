package com.online.shop.api;

import com.online.shop.api.exception.RestException;
import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.dto.UserDto;
import com.online.shop.service.abstraction.CartService;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.abstraction.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "/api/v1/carts")
public class CartRestController extends BaseRestController {

    private final CartService cartService;
    private final UserService userService;
    private final ItemService itemService;

    public CartRestController(CartService cartService, UserService userService, ItemService itemService) {
        this.cartService = cartService;
        this.userService = userService;
        this.itemService = itemService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemQuantityDto>> getItemsInCart(@PathVariable Long id) {
        UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
        if(user.getCart().getId().equals(id) || userService.isAdmin(user.getId())) {
            return ResponseEntity.ok(cartService.getItemsFromCart(id));
        }
        throw getPermissionRestException();
    }

    @PostMapping(value = "/{id}/{itemId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> addItemToCart(@PathVariable(name = "id") Long cartId,
                                                        @PathVariable(name = "itemId") Long itemId,
                                                        @RequestParam(name = "quantity", defaultValue = "1") Long quantity) {
        UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
        if(user.getCart().getId().equals(cartId)) {
            itemService.addItemToCart(user.getCart().getId(), itemId, quantity);
            return ResponseEntity.ok(new ResponseObject(true, "Successfully added item to cart."));
        }
        throw getPermissionRestException();
    }

    @PutMapping(value = "/{id}/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> updateItemInCart(@PathVariable(name = "id") Long cartId,
                                                        @PathVariable Long itemId,
                                                        @RequestParam(name = "quantity", defaultValue = "-1") Long quantityToAdd) {
        if(quantityToAdd > 0) {
            UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
            if (user.getCart().getId().equals(cartId)) {
                cartService.updateItemInCart(cartId, itemId, quantityToAdd);
                return ResponseEntity.ok(new ResponseObject(true, "Successfully added item to cart."));
            }
            throw getPermissionRestException();
        }
        throw new RestException("Quantity to add must be greater than zero.", HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id}/{itemId}")
    public ResponseEntity<ResponseObject> removeItemFromCart(@PathVariable Long id, @PathVariable Long itemId) {
        UserDto user = userService.getUserDtoByUsername(getCurrentUserUsername());
        if(user.getCart().getId().equals(id) || isAdmin(user.getAuthorities())) {
            if(cartService.removeItem(id, itemId)) {
                return ResponseEntity.ok(new ResponseObject(true, "Successfully deleted item from cart."));
            } else {
                return new ResponseEntity<>(new ResponseObject(true,
                        HttpStatus.NOT_FOUND.value(),
                        "Item not found."), HttpStatus.NOT_FOUND);
            }
        }
        throw getPermissionRestException();
    }

}

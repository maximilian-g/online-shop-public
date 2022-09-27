package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.PropertyValueDto;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.PropertyService;
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

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/api/v1/property_values")
@Validated
public class PropertyValueRestController extends BaseRestController {

    private final PropertyService propertyService;

    public PropertyValueRestController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyValueDto> getSinglePropertyVal(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropValueById(id));
    }

    @GetMapping(value = "/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemDto>> getSinglePropertyItems(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getItemsOfPropertyValue(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyValueDto> createPropertyVal(@RequestBody @Validated({New.class}) @Valid PropertyValueDto property) {
        return ResponseEntity.ok(propertyService.createPropertyVal(property.getValue(), property.getPropId()));
    }

    @PutMapping(value = "/{id}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> assignPropertyValItem(@PathVariable Long id,
                                                                  @PathVariable Long itemId) {
        propertyService.assignItemToPropertyVal(id, itemId);
        return ResponseEntity.ok(new ResponseObject(
                true,
                HttpStatus.OK.value(),
                "Successfully created item link."
        ));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyValueDto> updateProperty(@PathVariable Long id,
                                                           @RequestBody @Validated({Exists.class}) @Valid PropertyValueDto property) {
        return ResponseEntity.ok(propertyService.updatePropertyVal(id, property.getValue(), property.getPropId()));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> deleteProperty(@PathVariable Long id) {
        propertyService.deletePropertyValueById(id);
        return ResponseEntity.ok(new ResponseObject(true,
                HttpStatus.OK.value(),
                "Property value with id '" + id + "' successfully deleted."));
    }

    @DeleteMapping(value = "/{id}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> deletePropertyValItem(@PathVariable Long id,
                                                                  @PathVariable Long itemId) {
        propertyService.removeItemFromPropertyVal(id, itemId);
        return ResponseEntity.ok(new ResponseObject(
                true,
                HttpStatus.OK.value(),
                "Successfully removed item link."
        ));
    }

}

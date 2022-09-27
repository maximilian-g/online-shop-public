package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.PropertyDto;
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
@RequestMapping(path = "/api/v1/properties")
@Validated
public class PropertyRestController extends BaseRestController {

    private final PropertyService propertyService;

    public PropertyRestController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<PropertyDto>> getProperties() {
        return ResponseEntity.ok(propertyService.getAll());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyDto> getSingleProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyDto> createProperty(@RequestBody @Validated({New.class}) @Valid PropertyDto property) {
        return ResponseEntity.ok(propertyService.createProperty(property));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyDto> updateProperty(@PathVariable Long id,
                                                      @RequestBody @Validated({Exists.class}) @Valid PropertyDto property) {
        return ResponseEntity.ok(propertyService.updateProperty(id, property));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> deleteProperty(@PathVariable Long id) {
        propertyService.deletePropertyById(id);
        return ResponseEntity.ok(new ResponseObject(true,
                HttpStatus.OK.value(),
                "Property with id '" + id + "' successfully deleted."));
    }

}

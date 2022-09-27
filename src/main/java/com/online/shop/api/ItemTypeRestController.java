package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.ItemTypeDto;
import com.online.shop.dto.min.ItemTypeDtoMinimized;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.ItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/api/v1/item_types")
@Validated
public class ItemTypeRestController extends BaseRestController {

    private final ItemTypeService itemTypeService;

    @Autowired
    public ItemTypeRestController(ItemTypeService itemTypeService) {
        this.itemTypeService = itemTypeService;
    }

    @GetMapping(value = "/min", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemTypeDtoMinimized>> getAllItemTypesMin() {
        return ResponseEntity.ok(itemTypeService.findAllItemTypesMin());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemTypeDto>> getItemTypes(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                     @RequestParam(value = "size", defaultValue = "-1") Integer size) {
        if (size < 0) {
            size = itemTypeService.getPageSizeProp();
        }
        if(page < 0) {
            page = 1;
        }
        return ResponseEntity.ok(itemTypeService.findPaginated(size, page).page.content);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemTypeDto> getSingleItemType(@PathVariable Long id) {
        return ResponseEntity.ok(itemTypeService.getById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemTypeDto> createItemType(@RequestBody @Validated({New.class}) @Valid ItemTypeDto itemType) {
        return ResponseEntity.ok(itemTypeService.createItemType(itemType.getName(), itemType.getCategory().getId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemTypeDto> updateItemType(@PathVariable Long id,
            @RequestBody @Validated({Exists.class}) @Valid ItemTypeDto itemType) {
        return ResponseEntity.ok(itemTypeService.updateItemType(id, itemType.getName(), itemType.getCategory().getId()));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> deleteItemType(@PathVariable Long id) {
        itemTypeService.deleteItemType(id);
        return ResponseEntity.ok(new ResponseObject(true,
                HttpStatus.OK.value(),
                "Item type with id '" + id + "' successfully deleted."));
    }

}

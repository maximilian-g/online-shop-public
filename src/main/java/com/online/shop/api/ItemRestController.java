package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.ImageDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.impl.ItemBulkUploadService;
import com.online.shop.service.util.InputResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

import static com.online.shop.service.impl.CategoryServiceImpl.DEFAULT_CATEGORY_ID;
import static com.online.shop.service.impl.CategoryServiceImpl.DEFAULT_CATEGORY_ID_STR;
import static com.online.shop.service.impl.ItemTypeServiceImpl.DEFAULT_ITEM_TYPE_ID;

@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemRestController extends BaseRestController {

    private final ItemService itemService;
    private final ItemBulkUploadService itemBulkUploadService;

    @Autowired
    public ItemRestController(ItemService itemService, ItemBulkUploadService itemBulkUploadService) {
        this.itemService = itemService;
        this.itemBulkUploadService = itemBulkUploadService;
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadItemsJson() {

        InputResource allItemsAsJson = itemService.getAllItemsAsJsonResource();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "filename=" + allItemsAsJson.getResourceName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(allItemsAsJson.getContentLength())
                .body(allItemsAsJson.getInputStreamResource());
    }

    @GetMapping(value = "/min", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemDto>> getAllItemsMin() {
        return ResponseEntity.ok(itemService.findAllIdAndName());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ItemDto>> getItems(
            @RequestParam(value = "categoryId", defaultValue = DEFAULT_CATEGORY_ID_STR) Long category,
            @RequestParam(value = "itemTypeId", defaultValue = DEFAULT_CATEGORY_ID_STR) Long itemType,
            @RequestParam(value = "properties",
                    required = false,
                    defaultValue = ""/*empty list*/) List<Long> properties,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "-1") Integer size) {
        if (size < 0) {
            size = itemService.getPageSizeProp();
        }
        if(page < 0) {
            page = 1;
        }
        if(!DEFAULT_CATEGORY_ID.equals(category) && !DEFAULT_ITEM_TYPE_ID.equals(itemType)) {
            if(properties.isEmpty()) {
                return ResponseEntity.ok(itemService.findPaginatedByCategoryAndItemType(category, itemType, size, page).page.content);
            } else {
                return ResponseEntity.ok(itemService.findPaginatedByCategoryAndItemTypeAndPropValues(
                        category,
                        itemType,
                        properties,
                        size,
                        page
                ).page.content);
            }
        } else if(!DEFAULT_CATEGORY_ID.equals(category)) {
            return ResponseEntity.ok(itemService.findPaginatedByCategory(category, size, page).page.content);
        } else if (!DEFAULT_ITEM_TYPE_ID.equals(itemType)) {
            return ResponseEntity.ok(itemService.findPaginatedByItemType(itemType, size, page).page.content);
        }
        return ResponseEntity.ok(itemService.findPaginated(size, page).page.content);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDto> getSingleItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    @GetMapping(value = "/{id}/quantity")
    public ResponseEntity<Long> getItemQuantity(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemQuantityInStock(id));
    }

    @GetMapping(value = "/{id}/images")
    public ResponseEntity<Collection<ImageDto>> getItemImages(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemImages(id));
    }

    @PostMapping(value = "/bulk_upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> uploadItems(@RequestParam(name = "file") MultipartFile file) {
        List<String> logs = itemBulkUploadService.uploadItemsFile(file);
        return new ResponseEntity<>(logs, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDto> createItem(@Validated({New.class}) @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.createItem(itemDto));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDto> updateItem(@Validated({Exists.class}) @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.updateItem(itemDto, itemService.getItemQuantityInStock(itemDto.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteItem(@PathVariable Long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.ok(new ResponseObject(true, "Item successfully deleted."));
    }

}

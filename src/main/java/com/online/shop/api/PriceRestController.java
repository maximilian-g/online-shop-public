package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.PriceDto;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.service.impl.ItemPriceFacade;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/api/v1/prices")
public class PriceRestController extends BaseRestController {

    private final PriceService priceService;
    private final ItemPriceFacade itemPriceFacade;

    @Autowired
    public PriceRestController(PriceService priceService, ItemPriceFacade itemPriceFacade) {
        this.priceService = priceService;
        this.itemPriceFacade = itemPriceFacade;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceDto> getPriceById(@PathVariable Long id) {
        return ResponseEntity.ok(priceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PriceDto> createPrice(@Validated({New.class}) @RequestBody PriceDto priceDto) {
        return ResponseEntity.ok(priceService.getById(itemPriceFacade.createPriceAndAttachToItem(priceDto)));
    }

    @PutMapping
    public ResponseEntity<PriceDto> updatePrice(@Validated({Exists.class}) @RequestBody PriceDto priceDto) {
        return ResponseEntity.ok(priceService.getById(itemPriceFacade.updatePriceAndReattachToItem(priceDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deletePrice(@PathVariable Long id) {
        priceService.deletePriceById(id);
        return ResponseEntity.ok(new ResponseObject(true, "Price was successfully deleted"));
    }

}

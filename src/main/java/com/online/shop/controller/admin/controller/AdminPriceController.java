package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.PriceDto;
import com.online.shop.entity.Price;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.abstraction.PriceService;
import com.online.shop.service.impl.ItemPriceFacade;
import com.online.shop.service.impl.ItemServiceImpl;
import com.online.shop.service.impl.PriceServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PriceServiceImpl.PRICES_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PriceServiceImpl.SINGLE_PRICE_ATTRIBUTE_NAME;
import static com.online.shop.util.DateUtil.CURRENT_DATE_FORMAT;

@Controller
@RequestMapping(path = "/admin/price")
public class AdminPriceController extends BaseController {

    private final PriceService priceService;
    private final ItemService itemService;
    private final ItemPriceFacade itemPriceFacade;

    @Autowired
    protected AdminPriceController(UserServiceImpl userService, PriceServiceImpl priceService, ItemServiceImpl itemService, ItemPriceFacade itemPriceFacade) {
        super(userService, LoggerFactory.getLogger(AdminPriceController.class));
        this.priceService = priceService;
        this.itemService = itemService;
        this.itemPriceFacade = itemPriceFacade;
    }

    @GetMapping
    public String getPricesView(@RequestParam("page") Optional<Integer> page,
                                @RequestParam("size") Optional<Integer> size,
                                Model model) {
        addAuthAttribute(model);
        addPaginatedPricesToModel(model, size.orElse(priceService.getPageSizeProp()), page.orElse(1));
        return "adminPrices";
    }

    @GetMapping("/{id}")
    public String getSinglePrice(Model model,
                                 @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(SINGLE_PRICE_ATTRIBUTE_NAME, priceService.getById(id));
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());
        return "adminPrices";
    }

    @PostMapping
    public String createPrice(Model model,
                              @DateTimeFormat(pattern = CURRENT_DATE_FORMAT) Date startDate,
                              @DateTimeFormat(pattern = CURRENT_DATE_FORMAT) Date endDate,
                              @RequestParam(defaultValue = "false") boolean isOpenInterval,
                              BigDecimal price) {
        addAuthAttribute(model);
        PriceDto priceDto = new PriceDto();
        priceDto.setStartDate(startDate);
        if (!isOpenInterval) {
            priceDto.setEndDate(endDate);
        }
        priceDto.setPrice(price);
        Price entity = itemPriceFacade.createPrice(priceDto);
        model.addAttribute(SINGLE_PRICE_ATTRIBUTE_NAME, priceService.getById(entity.getId()));
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());
        return "adminPrices";
    }

    @PutMapping("/{id}")
    public String updatePrice(Model model,
                              @PathVariable Long id,
                              String startDate,
                              String endDate,
                              BigDecimal price,
                              @RequestParam(defaultValue = "false") Boolean reassign,
                              Long itemId) {
        addAuthAttribute(model);
        Long priceId = itemPriceFacade.updatePrice(id, itemId, reassign, startDate, endDate, price);
        model.addAttribute(SINGLE_PRICE_ATTRIBUTE_NAME, priceService.getById(priceId));
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());
        return "adminPrices";
    }

    @PatchMapping("/{id}")
    public String detachItemFromPrice(Model model,
                                      @PathVariable Long id) {
        addAuthAttribute(model);
        PriceDto price = priceService.detachItemFromPrice(id);
        model.addAttribute(SINGLE_PRICE_ATTRIBUTE_NAME, price);
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());
        return "adminPrices";

    }

    @DeleteMapping("/{id}")
    public String updatePrice(Model model,
                              @PathVariable Long id) {
        addAuthAttribute(model);
        priceService.deletePriceById(id);
        addPaginatedPricesToModel(model, priceService.getPageSizeProp(), 1);
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, "Successfully deleted price with id '" + id + "'");
        return "adminPrices";
    }

    private void addPaginatedPricesToModel(Model model, int pageSize, int currentPage) {
        Pagination<PriceDto> pagination = priceService.findPaginated(
                pageSize,
                currentPage);
        model.addAttribute(PRICES_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
    }

}

package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.ItemTypeDto;
import com.online.shop.service.abstraction.ItemTypeService;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.ITEM_TYPES_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.ITEM_TYPES_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.SINGLE_ITEM_TYPE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/admin/item_type")
public class AdminItemTypeController extends BaseController {

    private final ItemTypeService itemTypeService;

    @Autowired
    public AdminItemTypeController(ItemTypeService itemTypeService, UserService userService) {
        super(userService, LoggerFactory.getLogger(AdminItemTypeController.class));
        this.itemTypeService = itemTypeService;
    }

    @GetMapping
    public String getAllItemTypes(@RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model model) {
        addAuthAttribute(model);

        Pagination<ItemTypeDto> pagination = itemTypeService.findPaginated(size.orElse(itemTypeService.getPageSizeProp()), page.orElse(1));

        model.addAttribute(ITEM_TYPES_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
        model.addAttribute(ITEM_TYPES_ATTRIBUTE_NAME, itemTypeService.getAllItemTypes());
        return "adminItemTypes";
    }

    @GetMapping("/{id}")
    public String getItemTypeById(@PathVariable Long id,
                              Model model) {
        addAuthAttribute(model);
        model.addAttribute(SINGLE_ITEM_TYPE_ATTRIBUTE_NAME, itemTypeService.getById(id));
        return "adminItemTypes";
    }

}

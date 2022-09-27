package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.controller.admin.forms.ItemCreationForm;
import com.online.shop.dto.ItemDto;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.abstraction.ItemTypeService;
import com.online.shop.service.impl.ItemServiceImpl;
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

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemServiceImpl.ITEM_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.ITEM_TYPES_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.SINGLE_ITEM_TYPE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/admin/item")
public class AdminItemController extends BaseController {

    private final ItemService itemService;
    private final ItemTypeService itemTypeService;

    protected AdminItemController(UserServiceImpl userService, ItemServiceImpl itemService, ItemTypeService itemTypeService) {
        super(userService, LoggerFactory.getLogger(AdminItemController.class));
        this.itemService = itemService;
        this.itemTypeService = itemTypeService;
    }

    @GetMapping
    public String getAllItems(@RequestParam("itemTypeId") Optional<Long> itemType,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size,
                              Model model) {
        addAuthAttribute(model);
        if (itemType.isPresent()) {
            addItemsAndCategoriesToModel(model, itemService.findPaginatedByItemType(itemType.get(),
                    size.orElse(itemService.getPageSizeProp()), page.orElse(1)));
            model.addAttribute(SINGLE_ITEM_TYPE_ATTRIBUTE_NAME, itemTypeService.getById(itemType.get()));
        } else {
            addItemsAndCategoriesToModel(model, itemService.findPaginated(size.orElse(itemService.getPageSizeProp()),
                    page.orElse(1)));
        }
        return "adminItems";
    }

    @GetMapping("/{id}")
    public String getSingleItem(Model model,
                                @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(ITEM_TYPES_ATTRIBUTE_NAME, itemTypeService.getAllItemTypes());
        model.addAttribute(ItemServiceImpl.SINGLE_ITEM_ATTRIBUTE_NAME, itemService.getById(id));
        return "adminItems";

    }

    @PostMapping
    public String createItem(ItemCreationForm form,
                             Model model) {
        addAuthAttribute(model);
        ItemDto itemToSave = new ItemDto();
        itemToSave.setName(form.getItemName());
        itemToSave.setDescription(form.getDescription());
        itemToSave.setItemType(itemTypeService.getById(form.getItemTypeId()));
        itemToSave = itemService.createItem(itemToSave);
        model.addAttribute(ITEM_TYPES_ATTRIBUTE_NAME, itemTypeService.getAllItemTypes());
        model.addAttribute(ItemServiceImpl.SINGLE_ITEM_ATTRIBUTE_NAME, itemToSave);
        return "adminItems";
    }

    @PutMapping("/{id}")
    public String updateItem(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam Long itemTypeId,
                             @RequestParam(defaultValue = "false") Boolean changeItemType,
                             @RequestParam(defaultValue = "-1") Long quantity,
                             Model model) {
        addAuthAttribute(model);
        ItemDto item = itemService.getById(id);
        item.setName(name);
        item.setDescription(description);
        if(changeItemType) {
            item.setItemType(itemTypeService.getById(itemTypeId));
        }
        item = itemService.updateItem(item, quantity == -1 ? itemService.getItemQuantityInStock(item.getId()) : quantity);
        model.addAttribute(ITEM_TYPES_ATTRIBUTE_NAME, itemTypeService.getAllItemTypes());
        model.addAttribute(ItemServiceImpl.SINGLE_ITEM_ATTRIBUTE_NAME, item);
        return "adminItems";
    }

    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id,
                             Model model) {
        addAuthAttribute(model);
        itemService.deleteItemById(id);
        addItemsAndCategoriesToModel(model, itemService.findPaginated(itemService.getPageSizeProp(), 1));
        return "adminItems";
    }

    private void addItemsAndCategoriesToModel(Model model, Pagination<ItemDto> pagination) {
        model.addAttribute(ITEM_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
        model.addAttribute(ITEM_TYPES_ATTRIBUTE_NAME, itemTypeService.getAllItemTypes());
    }

}

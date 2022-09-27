package com.online.shop.controller;

import com.online.shop.controller.admin.forms.ItemsParameters;
import com.online.shop.dto.CategoryDto;
import com.online.shop.dto.ItemDto;
import com.online.shop.dto.ItemTypeDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.User;
import com.online.shop.service.abstraction.CategoryService;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.abstraction.ItemTypeService;
import com.online.shop.service.impl.PropertyServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.online.shop.service.abstraction.BaseService.ERROR_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.CategoryServiceImpl.CATEGORY_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.CategoryServiceImpl.DEFAULT_CATEGORY_ID;
import static com.online.shop.service.impl.CategoryServiceImpl.INFO_MESSAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.CategoryServiceImpl.SINGLE_CATEGORY_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemServiceImpl.ITEM_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemServiceImpl.SINGLE_ITEM_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.ItemTypeServiceImpl.DEFAULT_ITEM_TYPE_ID;
import static com.online.shop.service.impl.ItemTypeServiceImpl.SINGLE_ITEM_TYPE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.PROPERTIES_IDS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.PROPERTIES_MSG_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.PROPERTIES_MSG_STR_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/items")
public class ItemsController extends BaseController {

    public static final String SEARCH_BY_ALL_PROPS_MATCH = "searchByAll";

    private final ItemService itemService;
    private final ItemTypeService itemTypeService;
    private final CategoryService categoryService;
    private final PropertyServiceImpl propertyService;

    @Autowired
    public ItemsController(ItemService itemService,
                           UserServiceImpl userService,
                           ItemTypeService itemTypeService, CategoryService categoryService, PropertyServiceImpl propertyService) {
        super(userService, LoggerFactory.getLogger(ItemsController.class));
        this.itemService = itemService;
        this.itemTypeService = itemTypeService;
        this.categoryService = categoryService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public String getAllItems(ItemsParameters parameters,
                              Model model) {
        addAuthAttribute(model);
        Pagination<ItemDto> itemDtoPagination;

        String itemName = parameters.getName().trim();
        if(!itemName.isEmpty()) {
            itemDtoPagination = getPaginationByItemName(parameters);
        } else {
            itemDtoPagination = getPaginationByParams(parameters);
        }

        addPaginatedItemsToModel(model, itemDtoPagination);
        Collection<CategoryDto> allCategories = categoryService.getAllCategories();

        allCategories.forEach(c -> c.setItemsAssociated(itemService.getItemCountByCategoryId(c.getId())));

        if(!parameters.getErrorMsg().isBlank()) {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, parameters.getErrorMsg());
        }
        if(!parameters.getInfo().isBlank()) {
            model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME, parameters.getInfo());
        }
        if(!parameters.getProperties().isEmpty()) {
            List<String> stringRepresentation = propertyService.getStringRepresentationOf(parameters.getProperties());
            model.addAttribute(PROPERTIES_MSG_ATTRIBUTE_NAME,
                    stringRepresentation);
            model.addAttribute(PROPERTIES_MSG_STR_ATTRIBUTE_NAME,
                    String.join(",", stringRepresentation));
            String propIds = parameters.getProperties()
                    .stream()
                    .map(v -> v + "")
                    .collect(Collectors.joining(","));
            model.addAttribute(PROPERTIES_IDS_ATTRIBUTE_NAME, propIds);
        }
        model.addAttribute(CATEGORY_ATTRIBUTE_NAME, allCategories);
        model.addAttribute(SEARCH_BY_ALL_PROPS_MATCH, parameters.getSearchByAll());
        if (parameters.getCategoryId() != null) {
            CategoryDto categoryDto = categoryService.getById(parameters.getCategoryId());
            categoryDto.setItemsAssociated(itemService.getItemCountByCategoryId(categoryDto.getId()));
            model.addAttribute(SINGLE_CATEGORY_ATTRIBUTE_NAME, categoryDto);
        }
        if (parameters.getItemTypeId() != null) {
            ItemTypeDto itemTypeDto = itemTypeService.getById(parameters.getItemTypeId());
            model.addAttribute(SINGLE_ITEM_TYPE_ATTRIBUTE_NAME, itemTypeDto);
            model.addAttribute("itemTypeId", itemTypeDto.getItemTypeId());
            if(model.getAttribute(SINGLE_CATEGORY_ATTRIBUTE_NAME) == null) {
                CategoryDto categoryDto = categoryService.getById(itemTypeDto.getCategory().getId());
                categoryDto.setItemsAssociated(itemService.getItemCountByCategoryId(categoryDto.getId()));
                model.addAttribute(SINGLE_CATEGORY_ATTRIBUTE_NAME, categoryDto);
                model.addAttribute("categoryId", categoryDto.getId());
            }
        }
        return "items";
    }

    @GetMapping(path = "/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        addAuthAttribute(model);
        model.addAttribute(SINGLE_ITEM_ATTRIBUTE_NAME, itemService.getById(id));
        return "item";
    }

    @PostMapping(path = "/{id}")
    public String addItemToCart(@PathVariable Long id,
                                @RequestParam("categoryId") Optional<Long> category,
                                @RequestParam("itemTypeId") Optional<Long> itemType,
                                @RequestParam("page") Optional<Integer> page,
                                @RequestParam("size") Optional<Integer> size,
                                @RequestParam(value = "properties",
                                        required = false,
                                        defaultValue = ""/*empty list*/) List<Long> properties,
                                @RequestParam(name = "quantity", defaultValue = "1") Long quantity,
                                Model model) {
        addAuthAttribute(model);
        User user = getUserFromAuthentication();
        int currentPage = page.orElse(1);
        int currentPageSize = size.orElse(itemService.getPageSizeProp());
        long categoryValue = category.orElse(DEFAULT_CATEGORY_ID);
        long itemTypeVal = itemType.orElse(DEFAULT_ITEM_TYPE_ID);
        Map<String, String> attributes = new HashMap<>();
        if (user != null) {
            UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
            itemService.addItemToCart(userDto.getCart().getId(), attributes, id, quantity);
        } else {
            return "login";
        }

        String resultUrl = "/items?size=" + currentPageSize +
                "&page=" + currentPage +
                (categoryValue == DEFAULT_CATEGORY_ID ? "" : "&categoryId=" + categoryValue) +
                (itemTypeVal == DEFAULT_ITEM_TYPE_ID ? "" : "&itemTypeId=" + itemTypeVal) +
                (properties.isEmpty() ? "" : "&properties=" +
                                URLEncoder.encode(
                                        properties.stream().map(Object::toString).collect(Collectors.joining(",")),
                                        StandardCharsets.UTF_8));
        if(attributes.containsKey(ERROR_ATTRIBUTE_NAME)) {
            resultUrl += "&errorMessage=" + URLEncoder.encode(attributes.get(ERROR_ATTRIBUTE_NAME), StandardCharsets.UTF_8);
        }
        if(attributes.containsKey(INFO_MESSAGE_ATTRIBUTE_NAME)) {
            resultUrl += "&info=" + URLEncoder.encode(attributes.get(INFO_MESSAGE_ATTRIBUTE_NAME), StandardCharsets.UTF_8);
        }
        return "redirect:" + resultUrl;
    }

    private Pagination<ItemDto> getPaginationByItemName(ItemsParameters params) {
        int currentPage = params.getPage() == null ? 1 : params.getPage();
        int pageSize = params.getSize() == null ? itemService.getPageSizeProp() : params.getSize();
        if(params.getSearchByContains()) {
            return itemService.findByNameContaining(params.getName(), pageSize, currentPage);
        }
        return itemService.findByName(params.getName(), pageSize, currentPage);
    }

    private Pagination<ItemDto> getPaginationByParams(ItemsParameters params) {
        int currentPage = params.getPage() == null ? 1 : params.getPage();
        int pageSize = params.getSize() == null ? itemService.getPageSizeProp() : params.getSize();
        if(params.getCategoryId() != null && params.getItemTypeId() != null) {
            if (params.getProperties().isEmpty()) {
                return itemService.findPaginatedByCategoryAndItemType(
                        params.getCategoryId(),
                        params.getItemTypeId(),
                        pageSize,
                        currentPage
                );
            }
            if (!params.getSearchByAll()) {
                return itemService.findPaginatedByCategoryAndItemTypeAndPropValues(
                        params.getCategoryId(),
                        params.getItemTypeId(),
                        params.getProperties(),
                        pageSize,
                        currentPage);
            }
            return itemService.findPaginatedByCategoryAndItemTypeAndPropValuesStrict(
                    params.getItemTypeId(),
                    params.getProperties(),
                    pageSize,
                    currentPage
            );
        }
        if(params.getCategoryId() != null) {
            return itemService.findPaginatedByCategory(params.getCategoryId(), pageSize, currentPage);
        }
        if(params.getItemTypeId() != null) {
            return itemService.findPaginatedByItemType(params.getItemTypeId(), pageSize, currentPage);
        }
        return itemService.findPaginated(pageSize, currentPage);
    }

    private void addPaginatedItemsToModel(Model model, Pagination<ItemDto> pagination) {
        model.addAttribute(ITEM_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
    }

}

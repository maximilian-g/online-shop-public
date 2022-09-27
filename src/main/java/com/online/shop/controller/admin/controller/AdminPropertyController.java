package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.PropertyDto;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.impl.PropertyServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.PROPERTIES_PAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.SINGLE_PROPERTY_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.PropertyServiceImpl.SINGLE_PROPERTY_VAL_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/admin/property")
public class AdminPropertyController extends BaseController {

    private final PropertyServiceImpl propertyService;

    protected AdminPropertyController(UserService userService, PropertyServiceImpl propertyService) {
        super(userService, LoggerFactory.getLogger(AdminPropertyController.class));
        this.propertyService = propertyService;
    }

    @GetMapping
    public String getAllProperties(@RequestParam("page") Optional<Integer> page,
                                   @RequestParam("size") Optional<Integer> size,
                                   Model model) {
        addAuthAttribute(model);

        Pagination<PropertyDto> pagination = propertyService.findPaginated(size.orElse(propertyService.getPageSizeProp()), page.orElse(1));

        model.addAttribute(PROPERTIES_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
        return "adminProperties";
    }

    @GetMapping("/{id}")
    public String getSingleProperty(@PathVariable Long id,
                              Model model) {
        addAuthAttribute(model);
        model.addAttribute(SINGLE_PROPERTY_ATTRIBUTE_NAME, propertyService.getById(id));
        return "adminProperties";
    }

    @GetMapping("/value/{id}")
    public String getSinglePropertyValue(@PathVariable Long id,
                              Model model) {
        addAuthAttribute(model);
        model.addAttribute(SINGLE_PROPERTY_VAL_ATTRIBUTE_NAME, propertyService.getPropValueById(id));
        return "adminProperties";
    }

}

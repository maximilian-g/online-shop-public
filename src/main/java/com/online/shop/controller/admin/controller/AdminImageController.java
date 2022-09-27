package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.ImageDto;
import com.online.shop.service.abstraction.UserService;
import com.online.shop.service.impl.ImageServiceImpl;
import com.online.shop.service.impl.ItemServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;


@Controller
@RequestMapping(path = "/admin/image")
public class AdminImageController extends BaseController {

    private final ItemServiceImpl itemService;
    private final ImageServiceImpl imageService;

    protected AdminImageController(UserService userService, ItemServiceImpl itemService, ImageServiceImpl imageService) {
        super(userService, LoggerFactory.getLogger(AdminImageController.class));
        this.itemService = itemService;
        this.imageService = imageService;
    }

    @GetMapping
    public String getImagePage(Model model,
                               @RequestParam(name = "page", defaultValue = "-1") int page,
                               @RequestParam(name = "size", defaultValue = "-1") int pageSize) {
        addAuthAttribute(model);
        if(page < 0) {
            page = 1;
        }
        if(pageSize < 0) {
            pageSize = imageService.getPageSizeProp();
        }
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());

        Pagination<ImageDto> pagination = imageService.findPaginated(pageSize, page);
        model.addAttribute(ImageServiceImpl.IMAGES_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);

        return "adminImages";
    }

    @GetMapping("/{id}")
    public String getSingleImagePage(@PathVariable("id") long id,
                                     Model model) {
        addAuthAttribute(model);
        model.addAttribute(ItemServiceImpl.ITEMS_ATTRIBUTE_NAME, itemService.findAllIdAndName());
        model.addAttribute(ImageServiceImpl.IMAGE_ATTRIBUTE_NAME, imageService.getImageDtoById(id));
        return "adminImages";
    }

}

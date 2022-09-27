package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.dto.CategoryDto;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.CategoryService;
import com.online.shop.service.abstraction.ItemService;
import com.online.shop.service.impl.CategoryServiceImpl;
import com.online.shop.service.impl.UserServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@Controller
@RequestMapping(path = "/admin/category")
public class AdminCategoryController extends BaseController {

    private final CategoryService categoryService;
    private final ItemService itemService;

    @Autowired
    protected AdminCategoryController(UserServiceImpl userService, CategoryService categoryService, ItemService itemService) {
        super(userService, LoggerFactory.getLogger(AdminCategoryController.class));
        this.categoryService = categoryService;
        this.itemService = itemService;
    }

    @GetMapping
    public String getCategoryView(Model model) {
        addAuthAttribute(model);
        Collection<CategoryDto> allCategories = categoryService.getAllCategories();
        allCategories.forEach(c -> c.setItemsAssociated(itemService.getItemCountByCategoryId(c.getId())));
        model.addAttribute(CategoryServiceImpl.CATEGORY_ATTRIBUTE_NAME, allCategories);
        return "adminCategory";
    }

    @GetMapping("/{id}")
    public String getSingleCategory(Model model,
                                    @PathVariable Long id) {
        addAuthAttribute(model);
        CategoryDto singleCategory = categoryService.getById(id);
        singleCategory.setItemsAssociated(itemService.getItemCountByCategoryId(singleCategory.getId()));
        model.addAttribute(CategoryServiceImpl.SINGLE_CATEGORY_ATTRIBUTE_NAME, singleCategory);
        return "adminCategory";
    }

    @PostMapping
    public String createCategory(Model model, String categoryName) {
        addAuthAttribute(model);
        categoryService.createCategory(categoryName);
        return "redirect:/admin/category";
    }

    @DeleteMapping("/{id}")
    public String deleteCategory(Model model, @PathVariable Long id) {
        addAuthAttribute(model);
        categoryService.deleteById(id);
        Collection<CategoryDto> allCategories = categoryService.getAllCategories();
        allCategories.forEach(c -> c.setItemsAssociated(itemService.getItemCountByCategoryId(c.getId())));
        model.addAttribute(CategoryServiceImpl.CATEGORY_ATTRIBUTE_NAME, allCategories);
        return "adminCategory";
    }

    @PutMapping("/{id}")
    public String updateCategory(Model model, @PathVariable Long id, String newCategoryName) {
        addAuthAttribute(model);
        CategoryDto categoryDto = new CategoryDto(id, newCategoryName);
        categoryService.updateCategory(categoryDto);
        CategoryDto singleCategory = categoryService.getById(id);
        singleCategory.setItemsAssociated(itemService.getItemCountByCategoryId(singleCategory.getId()));
        model.addAttribute(CategoryServiceImpl.SINGLE_CATEGORY_ATTRIBUTE_NAME, singleCategory);
        model.addAttribute(BaseService.INFO_MESSAGE_ATTRIBUTE_NAME, "Category was successfully updated.");
        return "adminCategory";
    }

}

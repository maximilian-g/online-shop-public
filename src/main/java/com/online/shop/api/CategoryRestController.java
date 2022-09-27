package com.online.shop.api;

import com.online.shop.api.util.ResponseObject;
import com.online.shop.dto.CategoryDto;
import com.online.shop.dto.transfer.Exists;
import com.online.shop.dto.transfer.New;
import com.online.shop.service.abstraction.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategoryRestController extends BaseRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Validated({New.class}) @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto.getCategory()));
    }

    @PutMapping
    public ResponseEntity<CategoryDto> updateCategory(@Validated({Exists.class}) @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok(new ResponseObject(true, "Category was successfully deleted"));
    }

}

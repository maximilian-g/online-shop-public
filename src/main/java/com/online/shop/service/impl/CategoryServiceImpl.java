package com.online.shop.service.impl;

import com.online.shop.dto.CategoryDto;
import com.online.shop.entity.Category;
import com.online.shop.repository.CategoryRepository;
import com.online.shop.service.abstraction.BaseService;
import com.online.shop.service.abstraction.CategoryService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

@Service
@Transactional
public class CategoryServiceImpl extends BaseService implements CategoryService {

    private final CategoryRepository categoryRepository;

    public static final String CATEGORY_ATTRIBUTE_NAME = "categories";
    public static final String SINGLE_CATEGORY_ATTRIBUTE_NAME = "category";
    public static final Long DEFAULT_CATEGORY_ID = -1L;
    public static final String DEFAULT_CATEGORY_ID_STR = "-1";

    @Autowired
    protected CategoryServiceImpl(CategoryRepository categoryRepository, Validator validator) {
        super(validator, LoggerFactory.getLogger(CategoryServiceImpl.class));
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = getCategoryById(id);
        return CategoryDto.getCategoryDto(category);
    }

    @Override
    public CategoryDto createCategory(String name) {
        Category newCategory = new Category();
        newCategory.setName(name);
        return CategoryDto.getCategoryDto(addCategory(newCategory));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto editedCategory) {
        Category resultCategory = updateCategory(editedCategory.getId(), editedCategory.getCategory());
        return CategoryDto.getCategoryDto(resultCategory);
    }

    @Override
    public Collection<CategoryDto> getAllCategories() {
        Collection<Category> allCategories = categoryRepository.findAll();
        return CategoryDto.getCategoryDtoCollection(allCategories);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityUpdateException("Cannot delete non existent category."));
        if (category.getItemTypes().size() == 0) {
            categoryRepository.delete(category);
        } else {
            throw new EntityUpdateException("Cannot delete category that is currently in associated to some item types.");
        }
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public CategoryDto getByName(String name) {
        return CategoryDto.getCategoryDto(getCategoryByName(name));
    }

    public Category addCategory(Category category) {
        Set<ConstraintViolation<Category>> violations = getViolations(category);
        if (violations.isEmpty()) {
            if (!categoryRepository.existsByName(category.getName())) {
                return categoryRepository.save(category);
            } else {
                throw new EntityUpdateException("Category with name '" + category.getName() + "' already exists.");
            }
        } else {
            throw new EntityUpdateException(getErrorMessagesTotal(violations));
        }
    }

    public Category updateCategory(Long id, String newCategoryName) {
        Category category = getCategoryById(id);
        Category validationCategory = new Category();
        validationCategory.setName(newCategoryName);
        Set<ConstraintViolation<Category>> violations = getViolations(validationCategory);
        if (violations.isEmpty()) {
            if (!categoryRepository.existsByName(newCategoryName)) {
                category.setName(newCategoryName);
                return categoryRepository.save(category);
            } else {
                throw new EntityUpdateException("Category with name '" + category.getName() + "' already exists.");
            }
        } else {
            throw new EntityUpdateException(getErrorMessagesTotal(violations));
        }
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id #" + id + " was not found."));
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findCategoryByName(name)
                .orElseThrow(() -> new NotFoundException("Category with name '" + name + "' was not found."));
    }


}

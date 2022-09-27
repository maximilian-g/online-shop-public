package com.online.shop.service.abstraction;

import com.online.shop.dto.CategoryDto;

import java.util.Collection;

public interface CategoryService {

    CategoryDto getById(Long id);

    CategoryDto createCategory(String name);

    CategoryDto updateCategory(CategoryDto editedCategory);

    Collection<CategoryDto> getAllCategories();

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByName(String name);

    CategoryDto getByName(String name);

}

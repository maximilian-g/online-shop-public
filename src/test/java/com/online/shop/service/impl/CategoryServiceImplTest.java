package com.online.shop.service.impl;

import com.online.shop.dto.CategoryDto;
import com.online.shop.entity.Category;
import com.online.shop.entity.ItemType;
import com.online.shop.repository.CategoryRepository;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

// is needed to properly operate with mocks
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        categoryService = new CategoryServiceImpl(categoryRepository, validator);
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Car parts");
        category.setAssociatedItemsCount(0);
        category.setItemTypes(Collections.emptyList());
        return category;
    }

    @Test
    void getById() {
        Category category = getCategory();
        BDDMockito.given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        BDDMockito.given(categoryRepository.findById(2L)).willReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> categoryService.getById(category.getId()));
        CategoryDto categoryDto = categoryService.getById(category.getId());
        Assertions.assertNotNull(categoryDto.getId());
        Assertions.assertNotNull(categoryDto.getCategory());
        Assertions.assertEquals(0L, categoryDto.getItemsAssociated());
        Assertions.assertThrows(NotFoundException.class, () -> categoryService.getById(2L));
    }

    @Test
    void createCategory() {
        String newCategoryName = "Laptops";
        String existentCategoryName = "Keyboards";
        Mockito.when(categoryRepository.save(any(Category.class))).then((Answer<Category>) invocationOnMock -> {
            Category category = invocationOnMock.getArgument(0);
            category.setId(1L);
            category.setItemTypes(Collections.emptyList());
            return category;
        });
        BDDMockito.given(categoryRepository.existsByName(newCategoryName)).willReturn(false);
        BDDMockito.given(categoryRepository.existsByName(existentCategoryName)).willReturn(true);
        Assertions.assertDoesNotThrow(() -> categoryService.createCategory(newCategoryName));

        CategoryDto categoryDto = categoryService.createCategory(newCategoryName);
        Assertions.assertNotNull(categoryDto.getId());
        Assertions.assertNotNull(categoryDto.getCategory());
        Assertions.assertEquals(0L, categoryDto.getItemsAssociated());

        Assertions.assertThrows(EntityUpdateException.class, () -> categoryService.createCategory(existentCategoryName));
        Assertions.assertThrows(EntityUpdateException.class, () -> categoryService.createCategory(null));
    }

    @Test
    void updateCategory() {
        Category category = getCategory();
        CategoryDto dto = new CategoryDto(category.getId(), "Edited category");

        BDDMockito.given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(any(Category.class))).then(AdditionalAnswers.returnsFirstArg());
        BDDMockito.given(categoryRepository.existsByName(dto.getCategory())).willReturn(false);
        BDDMockito.given(categoryRepository.existsByName("Laptops")).willReturn(true);

        Assertions.assertDoesNotThrow(() -> categoryService.updateCategory(dto));
        Assertions.assertEquals(dto.getCategory(), category.getName());

        CategoryDto anotherDto = new CategoryDto(category.getId(), "Laptops");
        Assertions.assertThrows(EntityUpdateException.class, () -> categoryService.updateCategory(anotherDto));

        CategoryDto invalidDto = new CategoryDto(category.getId(), null);
        Assertions.assertThrows(EntityUpdateException.class, () -> categoryService.updateCategory(invalidDto));
    }

    @Test
    void getAllCategories() {
        Category category = getCategory();
        BDDMockito.given(categoryRepository.findAll()).willReturn(List.of(category));
        Collection<CategoryDto> categoryDtos = categoryService.getAllCategories();
        Assertions.assertEquals(1, categoryDtos.size());
        for(CategoryDto dto : categoryDtos) {
            Assertions.assertNotNull(dto.getId());
            Assertions.assertNotNull(dto.getCategory());
            Assertions.assertEquals(0L, dto.getItemsAssociated());
        }
    }

    @Test
    void deleteById() {

        Category category = getCategory();
        Category categoryWithItems = getCategory();
        // to check if "category.getItemTypes().size()" is called on delete of category
        categoryWithItems.setItemTypes(List.of(new ItemType(), new ItemType()));
        categoryWithItems.setId(2L);

        BDDMockito.given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        BDDMockito.given(categoryRepository.findById(categoryWithItems.getId())).willReturn(Optional.of(categoryWithItems));
        doNothing().when(categoryRepository).delete(any(Category.class));

        Assertions.assertThrows(EntityUpdateException.class, () -> categoryService.deleteById(categoryWithItems.getId()));

        Assertions.assertDoesNotThrow(() -> categoryService.deleteById(category.getId()));
        ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        // capturing an item
        Mockito.verify(categoryRepository).delete(categoryArgumentCaptor.capture());
        Category captured = categoryArgumentCaptor.getValue();
        Assertions.assertSame(captured, category);

    }
}
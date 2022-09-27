package com.online.shop.repository;

import com.online.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Optional<Category> findCategoryByName(String name);

}

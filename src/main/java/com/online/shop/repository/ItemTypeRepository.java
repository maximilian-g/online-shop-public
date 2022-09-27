package com.online.shop.repository;

import com.online.shop.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {

    boolean existsByName(String name);

    Optional<ItemType> findItemTypeByName(String name);

}

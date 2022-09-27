package com.online.shop.repository;

import com.online.shop.entity.ItemPropertyValueEntity;
import com.online.shop.entity.ItemPropertyValuePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPropertyValueRepository extends JpaRepository<ItemPropertyValueEntity, ItemPropertyValuePrimaryKey> {
}

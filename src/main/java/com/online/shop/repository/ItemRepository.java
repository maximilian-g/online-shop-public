package com.online.shop.repository;

import com.online.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.type.category.id = :id")
    Page<Item> getItemsByCategoryId(Long id, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.type.id = :id")
    Page<Item> getItemsByItemTypeId(Long id, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.type.category.id = :categoryId AND i.type.id = :itemTypeId")
    Page<Item> getItemsByCategoryIdAndItemTypeId(Long categoryId, Long itemTypeId, Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i, IN(i.propertyValues) pv WHERE i.type.category.id = :categoryId AND i.type.id = :itemTypeId AND pv.propertyValue.id IN(:propertyValueIds)")
    Page<Item> getItemsByCategoryIdAndItemTypeIdAndPropertyValIds(Long categoryId,
                                                                  Long itemTypeId,
                                                                  List<Long> propertyValueIds,
                                                                  Pageable pageable);

    Page<Item> findByNameContaining(String strToContain, Pageable pageable);

    Page<Item> findByName(String name, Pageable pageable);

    // selects item ids where property values of item are strictly equal to given list
    @Query(value = "SELECT DISTINCT(i.item_id) " +
            "FROM items i " +
            "INNER JOIN items_properties ip " +
            "ON i.item_id = ip.item_id " +
            "WHERE i.item_Type_id = :itemTypeId " +
            "AND :propertyIdsSizeToMatch = (SELECT count(i2.item_id) FROM items i2 INNER JOIN items_properties ip2 " +
            "ON i2.item_id = ip2.item_id WHERE i2.item_id = i.item_id AND ip2.property_value_id IN (:propertyValueIds))",
            nativeQuery = true)
    List<Long> getItemIdsByPropertyValueIdsStrict(List<Long> propertyValueIds,
                                                  Long propertyIdsSizeToMatch,
                                                  Long itemTypeId);

    @Query("SELECT count(i) FROM Item i WHERE i.type.category.id = :categoryId")
    long getItemsCountByCategoryId(Long categoryId);

    @Query("SELECT count(i) FROM Item i WHERE i.type.id = :itemTypeId")
    long getItemsCountByItemTypeId(Long itemTypeId);

    @Query(value = "SELECT * FROM items i " +
            "ORDER BY " +
            "(SELECT count(*) FROM orders_items oi WHERE oi.item_id = i.item_id) " +
            "DESC LIMIT :limit", nativeQuery = true)
    Collection<Item> findTopByOrders(Long limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> getItemByIdForUpdate(Long id);

}

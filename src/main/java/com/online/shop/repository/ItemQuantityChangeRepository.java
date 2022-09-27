package com.online.shop.repository;

import com.online.shop.entity.ItemQuantityChange;
import com.online.shop.repository.data.ItemQuantityDiffOnPeriodView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;

public interface ItemQuantityChangeRepository extends JpaRepository<ItemQuantityChange, Long> {

    @Query("SELECT SUM(iqc.change) FROM ItemQuantityChange iqc WHERE iqc.item.id = :itemId")
    Long getItemQuantity(Long itemId);

    @Query("SELECT SUM(iqc.change) " +
            "FROM ItemQuantityChange iqc " +
            "WHERE iqc.item.id = :itemId " +
            "AND iqc.changeDate < :date")
    Long getItemQuantityOnDate(Long itemId, Date date);

    @Query(value = "SELECT " +
            "iqc.item_id AS itemId, " +
            "i.name AS itemName, " +
            "sum(iqc.change_value) AS quantityOnStartDate, " +
            "iqcEnd.quantity AS quantityOnEndDate, " +
            "(iqcEnd.quantity - sum(iqc.change_value)) AS difference " +
            "FROM items_quantity_changes iqc, " +
            "     (SELECT innerIqc.item_id, sum(innerIqc.change_value) quantity " +
            "      FROM items_quantity_changes innerIqc " +
            "      WHERE innerIqc.change_date < :endDate " +
            "      GROUP BY innerIqc.item_id) iqcEnd, " +
            "      items i " +
            "WHERE iqc.change_date < :startDate " +
            "AND iqc.item_id = iqcEnd.item_id " +
            "AND iqc.item_id = i.item_id " +
            "GROUP BY iqc.item_id", nativeQuery = true)
    Collection<ItemQuantityDiffOnPeriodView> getItemQuantityChangeOnPeriod(Date startDate, Date endDate);

}

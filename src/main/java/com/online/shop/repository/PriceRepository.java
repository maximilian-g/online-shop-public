package com.online.shop.repository;

import com.online.shop.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Price p WHERE p.id = :id")
    Optional<Price> getPriceByIdForUpdate(Long id);

}

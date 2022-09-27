package com.online.shop.repository;

import com.online.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.id = :id")
    Optional<Cart> getCartByIdForUpdate(Long id);

}

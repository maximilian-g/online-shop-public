package com.online.shop.repository;

import com.online.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<Long> getIdByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.lastAccessDate >= :startDate AND u.lastAccessDate <= :endDate")
    Collection<User> getUsersActiveBetween(Date startDate, Date endDate);

}

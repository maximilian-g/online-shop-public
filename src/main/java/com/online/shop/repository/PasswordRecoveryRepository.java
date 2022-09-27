package com.online.shop.repository;

import com.online.shop.entity.PasswordRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, String> {
}

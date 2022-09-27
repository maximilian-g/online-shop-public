package com.online.shop.repository;

import com.online.shop.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.imagePath = :fileName")
    Optional<Image> getImageByName(String fileName);

    boolean existsByImagePath(String imagePath);

}

package com.example.com.venom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.com.venom.entity.MenuEntity;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    Optional<MenuEntity> findByRestaurantId(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);
}

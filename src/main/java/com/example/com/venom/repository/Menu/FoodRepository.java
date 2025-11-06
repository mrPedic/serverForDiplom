package com.example.com.venom.repository.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.Menu.FoodEntity;

@Repository
public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    /** Найти все блюда, принадлежащие определенной группе. */
    List<FoodEntity> findByFoodGroupId(Long foodGroupId); 
}
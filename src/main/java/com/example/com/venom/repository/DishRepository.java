package com.example.com.venom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.com.venom.entity.DishEntity;

public interface DishRepository extends JpaRepository<DishEntity, Long> {
}

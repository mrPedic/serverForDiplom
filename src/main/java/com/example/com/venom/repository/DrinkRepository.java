package com.example.com.venom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.com.venom.entity.DrinkEntity;

public interface DrinkRepository extends JpaRepository<DrinkEntity, Long> {
}

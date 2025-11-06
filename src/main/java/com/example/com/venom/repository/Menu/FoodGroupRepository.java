package com.example.com.venom.repository.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.Menu.FoodGroupEntity;

@Repository
public interface FoodGroupRepository extends JpaRepository<FoodGroupEntity, Long> {
    /** Найти все группы блюд, принадлежащие определенному заведению. */
    List<FoodGroupEntity> findByEstablishmentId(Long establishmentId);
}

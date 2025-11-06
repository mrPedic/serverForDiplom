package com.example.com.venom.repository.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.Menu.DrinkEntity;

@Repository
public interface DrinkRepository extends JpaRepository<DrinkEntity, Long> {
    /** Найти все напитки, принадлежащие определенной группе. */
    List<DrinkEntity> findByDrinkGroupId(Long drinkGroupId);
}
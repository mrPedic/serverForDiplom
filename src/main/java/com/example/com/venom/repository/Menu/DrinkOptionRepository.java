package com.example.com.venom.repository.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.Menu.DrinkOptionEntity;

@Repository
public interface DrinkOptionRepository extends JpaRepository<DrinkOptionEntity, Long> {
    /** Найти все опции (размер/цена) для конкретного напитка. */
    List<DrinkOptionEntity> findByDrinkId(Long drinkId); 
}
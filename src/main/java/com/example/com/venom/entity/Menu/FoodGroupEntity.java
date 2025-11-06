package com.example.com.venom.entity.Menu;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2. Сущность Группы Блюд (FoodGroup)
 */
@Entity
@Table(name = "menu_food_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Внешний ключ: к какому заведению относится
    private Long establishmentId; 
    
    private String name;
}
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
 * 5. Сущность Опции Напитка (DrinkOption)
 */
@Entity
@Table(name = "menu_drink_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrinkOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Внешний ключ: к какому напитку относится
    private Long drinkId; 

    private Integer sizeMl;
    private Double cost;
}
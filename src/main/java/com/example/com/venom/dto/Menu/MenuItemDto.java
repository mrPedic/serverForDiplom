package com.example.com.venom.dto.Menu;

import lombok.Data;

/**
 * Базовый DTO для общих свойств (не используем интерфейс в Entity/DTO)
 */
@Data
public abstract class MenuItemDto {
    private Long id;
    private String name;
    private Double caloriesPer100g;
    private Double fatPer100g;
    private Double carbohydratesPer100g;
    private Double proteinPer100g;
    private String ingredients;
    private String photoBase64;
}
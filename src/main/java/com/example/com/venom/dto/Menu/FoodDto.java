package com.example.com.venom.dto.Menu;

import lombok.Data;

/**
 * DTO для Еды
 */
@Data
public class FoodDto extends MenuItemDto {
    private Long foodGroupId; // FK на FoodGroup
    private Double cost;
    private Integer weight;
}
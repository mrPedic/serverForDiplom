package com.example.com.venom.dto.Menu;

import java.util.List;
import lombok.Data;

/**
 * DTO для Напитка
 */
@Data
public class DrinkDto extends MenuItemDto {
    private Long drinkGroupId; // FK на DrinksGroup
    private List<DrinkOptionDto> options;
}
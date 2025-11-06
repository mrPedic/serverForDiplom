package com.example.com.venom.dto.Menu;

import lombok.Data;

/**
 * Класс для представления цены/объема напитка
 */
@Data
public class DrinkOptionDto {
    private Long id; // ID опции (присваивается сервером)
    private Long drinkId; // FK на Drink
    private Integer sizeMl;
    private Double cost;
}
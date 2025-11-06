package com.example.com.venom.dto.Menu;

import java.util.List;

import lombok.Data;

/**
 * DTO для Группы Напитков
 */
@Data
public class DrinksGroupDto {
    private Long id;
    private Long establishmentId; // FK на Establishment
    private String name;
    private List<DrinkDto> items; // Используем DTO
}
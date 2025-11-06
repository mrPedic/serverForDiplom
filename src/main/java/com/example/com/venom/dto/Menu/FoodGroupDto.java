package com.example.com.venom.dto.Menu;

import java.util.List;

import lombok.Data;

/**
 * DTO для Группы Еды
 */
@Data
public class FoodGroupDto {
    private Long id;
    private Long establishmentId; // FK на Establishment
    private String name;
    private List<FoodDto> items; // Используем DTO
}
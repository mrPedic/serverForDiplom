package com.example.com.venom.dto.Menu;

import java.util.List;

import lombok.Data;

/**
 * Главный DTO для полного Меню
 */
@Data
public class MenuOfEstablishmentDto {
    private Long establishmentId;
    private List<FoodGroupDto> foodGroups;
    private List<DrinksGroupDto> drinksGroups;
}
package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.MenuItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemDto {
    @NotNull(message = "ID позиции меню обязательно")
    private Long menuItemId;

    @NotNull(message = "Тип позиции обязателен")
    private MenuItemType menuItemType;

    @Min(value = 1, message = "Количество должно быть не меньше 1")
    private Integer quantity;

    private Map<String, String> selectedOptions; // Для напитков
}
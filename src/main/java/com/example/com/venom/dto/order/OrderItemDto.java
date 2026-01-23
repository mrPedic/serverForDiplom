package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.MenuItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    private Long orderId;
    private Long menuItemId;
    private String menuItemName;
    private MenuItemType menuItemType;
    private Integer quantity;
    private double pricePerUnit;
    private double totalPrice;
    private Map<String, String> options; // Для напитков (JSON)
}
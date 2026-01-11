package com.example.com.venom.service.order;

import com.example.com.venom.dto.Menu.DrinkDto;
import com.example.com.venom.dto.Menu.DrinkOptionDto;
import com.example.com.venom.dto.Menu.FoodDto;
import com.example.com.venom.dto.order.CreateOrderItemDto;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderPriceCalculator {

    private final MenuService menuService;

    public OrderPriceCalculator(MenuService menuService) {
        this.menuService = menuService;
    }

    public double calculateTotalPrice(List<CreateOrderItemDto> items, EstablishmentEntity establishment) {
        double total = 0.0;

        for (CreateOrderItemDto item : items) {
            double itemPrice = calculateItemPrice(item, establishment);
            total += itemPrice * item.getQuantity();
        }

        // Добавляем стоимость доставки (если нужно)
        if (total < 1000) { // Бесплатная доставка от 1000 рублей
            total += 200; // Стоимость доставки
        }

        return total;
    }

    private double calculateItemPrice(CreateOrderItemDto item, EstablishmentEntity establishment) {
        Object menuItem = menuService.getMenuItemById(
                establishment.getId(),
                item.getMenuItemId(),
                item.getMenuItemType()
        );

        if (menuItem instanceof FoodDto) {
            return ((FoodDto) menuItem).getCost();
        } else if (menuItem instanceof DrinkDto) {
            DrinkDto drink = (DrinkDto) menuItem;
            if (item.getSelectedOptions() != null && item.getSelectedOptions().containsKey("size")) {
                String sizeStr = item.getSelectedOptions().get("size");
                int size = Integer.parseInt(sizeStr);

                return drink.getOptions().stream()
                        .filter(opt -> opt.getSizeMl() == size)
                        .map(DrinkOptionDto::getCost)
                        .findFirst()
                        .orElse(drink.getOptions().get(0).getCost());
            }
            return drink.getOptions().get(0).getCost();
        }

        return 0.0;
    }
}
package com.example.com.venom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.entity.MenuEntity;
import com.example.com.venom.repository.MenuRepository;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuRepository menuRepository;

    @GetMapping
    public ResponseEntity<String> getMenu(@PathVariable Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId)
                .map(menu -> ResponseEntity.ok(menu.getMenuJson()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Создать/обновить меню целиком (JSON)
    @PostMapping
    public ResponseEntity<MenuEntity> saveOrUpdateMenu(
            @PathVariable Long restaurantId,
            @RequestBody String menuJson) {

        MenuEntity menu = menuRepository.findByRestaurantId(restaurantId)
                .orElse(MenuEntity.builder().restaurantId(restaurantId).build());

        menu.setMenuJson(menuJson);
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMenu(@PathVariable Long restaurantId) {
        menuRepository.deleteByRestaurantId(restaurantId);
        return ResponseEntity.noContent().build();
    }

    // Добавить группу еды
    @PostMapping("/food-groups")
    public ResponseEntity<String> addFoodGroup(
            @PathVariable Long restaurantId,
            @RequestBody String foodGroupJson) {
        // Тут можно достать menuJson, распарсить через Jackson и вставить новую группу
        // Пока просто возвращаем успех
        return ResponseEntity.ok("Food group added for restaurant " + restaurantId);
    }

    // Добавить группу напитков
    @PostMapping("/drink-groups")
    public ResponseEntity<String> addDrinkGroup(
            @PathVariable Long restaurantId,
            @RequestBody String drinkGroupJson) {
        return ResponseEntity.ok("Drink group added for restaurant " + restaurantId);
    }
}

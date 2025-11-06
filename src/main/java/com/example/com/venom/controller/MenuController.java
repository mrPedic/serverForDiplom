package com.example.com.venom.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.com.venom.dto.Menu.DrinkDto;
import com.example.com.venom.dto.Menu.DrinksGroupDto;
import com.example.com.venom.dto.Menu.FoodDto;
import com.example.com.venom.dto.Menu.FoodGroupDto;
import com.example.com.venom.dto.Menu.MenuOfEstablishmentDto;
import com.example.com.venom.service.MenuService;

import lombok.RequiredArgsConstructor;

/**
 * Контроллер для всех операций, связанных с меню заведения.
 * Базовый путь: /api/v1/menu
 */
@RestController
@RequestMapping("/menu") 
@RequiredArgsConstructor
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);
    private final MenuService menuService;

    // --- 1. READ: Получить все меню ---
    
    /**
     * Возвращает полное меню (все группы еды и напитков) для заведения.
     * GET /menu/establishment/{establishmentId}
     */
    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<MenuOfEstablishmentDto> getMenu(
        @PathVariable Long establishmentId) {
        
        log.info("--- [GET /menu/establishment/{}] Fetching full menu.", establishmentId);
        
        MenuOfEstablishmentDto menu = menuService.getMenuByEstablishmentId(establishmentId);
        
        return ResponseEntity.ok(menu);
    }
    
    // -----------------------------------------------------------------
    // --- 2. ГРУППЫ ЕДЫ (FoodGroup) ---
    // -----------------------------------------------------------------

    /**
     * Создание новой группы еды.
     * POST /menu/group/food
     */
    @PostMapping("/group/food")
    public ResponseEntity<FoodGroupDto> createFoodGroup(@RequestBody FoodGroupDto dto) {
        if (dto.getEstablishmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EstablishmentId обязателен при создании группы.");
        }
        log.info("--- [POST /menu/group/food] Creating new food group for establishment: {}", dto.getEstablishmentId());
        FoodGroupDto created = menuService.saveFoodGroup(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    

    /**
    * Обновление существующей группы еды.
    * PUT /menu/group/food/{groupId}
    */
    @PutMapping("/group/food/{groupId}")
    public ResponseEntity<FoodGroupDto> updateFoodGroup(
       @PathVariable Long groupId, 
        @RequestBody FoodGroupDto dto) {
            
        // --- ИСПРАВЛЕНО: Устанавливаем ID из пути, игнорируя ID в теле.
        // Это гарантирует, что сервис будет искать по правильному ID.
        dto.setId(groupId); 
    
        log.info("--- [PUT /menu/group/food/{}] Updating food group.", groupId);
    
        // Вызываем saveFoodGroup, который теперь будет обрабатывать UPDATE
       FoodGroupDto updated = menuService.saveFoodGroup(dto);
        return ResponseEntity.ok(updated);
    }
    
   // -----------------------------------------------------------------
    // --- 3. ГРУППЫ НАПИТКОВ (DrinksGroup) ---
    // -----------------------------------------------------------------
    
    /**
     * Создание новой группы напитков.
     * POST /menu/drink/group
     */
    @PostMapping("/drink/group")
    public ResponseEntity<DrinksGroupDto> createDrinksGroup(@RequestBody DrinksGroupDto dto) {
        if (dto.getEstablishmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EstablishmentId обязателен при создании группы.");
        }
        // 🌟 ДОБАВЬТЕ ЭТОТ ЛОГ
        log.info("--- [POST /menu/drink/group] Creating new drink group for establishment: {}", dto.getEstablishmentId()); 
        // Если вы не видите этот лог на сервере, запрос не дошел до контроллера.
        
        DrinksGroupDto created = menuService.saveDrinksGroup(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    /**
     * Обновление существующей группы напитков.
     * PUT /menu/drink/group/{groupId}
     */
    @PutMapping("/drink/group/{groupId}") // ИСПРАВЛЕНО
    public ResponseEntity<DrinksGroupDto> updateDrinksGroup(
        @PathVariable Long groupId, 
        @RequestBody DrinksGroupDto dto) {
        
        if (!groupId.equals(dto.getId())) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID в пути не совпадает с ID в теле.");
        }
        log.info("--- [PUT /menu/drink/group/{}] Updating drink group.", groupId);
        DrinksGroupDto updated = menuService.saveDrinksGroup(dto);
        return ResponseEntity.ok(updated);
    }
    
    // -----------------------------------------------------------------
    // --- 4. DELETE: Группа (Удаление FoodGroup или DrinksGroup) ---
    // -----------------------------------------------------------------
    
    /**
     * Удаляет группу (еды или напитков) и все её компоненты.
     * DELETE /menu/group/{groupId}?isFood=true/false
     */
    @DeleteMapping("/group/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deleteGroup(
        @PathVariable Long groupId,
        @RequestParam boolean isFood) {

        log.info("--- [DELETE /menu/group/{}] Deleting group (isFood: {}).", groupId, isFood);
        menuService.deleteGroup(groupId, isFood);
    }

    // -----------------------------------------------------------------
    // --- 5. ЕДА (Food) ---
    // -----------------------------------------------------------------

    /**
     * Создание нового блюда.
     * POST /menu/item/food
     */
    @PostMapping("/item/food")
    public ResponseEntity<FoodDto> createFoodItem(@RequestBody FoodDto dto) {
        if (dto.getFoodGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FoodGroupId обязателен.");
        }
        log.info("--- [POST /menu/item/food] Creating new food item in group: {}", dto.getFoodGroupId());
        FoodDto created = menuService.saveFood(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    /**
     * Обновление существующего блюда.
     * PUT /menu/item/food/{itemId}
     */
    @PutMapping("/item/food/{itemId}")
    public ResponseEntity<FoodDto> updateFoodItem(
        @PathVariable Long itemId,
        @RequestBody FoodDto dto) {

        if (!itemId.equals(dto.getId())) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID в пути не совпадает с ID в теле.");
        }
        log.info("--- [PUT /menu/item/food/{}] Updating food item.", itemId);
        FoodDto updated = menuService.saveFood(dto);
        return ResponseEntity.ok(updated);
    }

    // -----------------------------------------------------------------
    // --- 6. НАПИТКИ (Drink) ---
    // -----------------------------------------------------------------
    
    /**
     * Создание нового напитка (включая его опции).
     * POST /menu/item/drink
     */
    @PostMapping("/item/drink")
    public ResponseEntity<DrinkDto> createDrinkItem(@RequestBody DrinkDto dto) {
        if (dto.getDrinkGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DrinkGroupId обязателен.");
        }
        log.info("--- [POST /menu/item/drink] Creating new drink item in group: {}", dto.getDrinkGroupId());
        DrinkDto created = menuService.saveDrink(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    /**
     * Обновление существующего напитка (включая его опции).
     * PUT /menu/item/drink/{itemId}
     */
    @PutMapping("/item/drink/{itemId}")
    public ResponseEntity<DrinkDto> updateDrinkItem(
        @PathVariable Long itemId,
        @RequestBody DrinkDto dto) {

        if (!itemId.equals(dto.getId())) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID в пути не совпадает с ID в теле.");
        }
        log.info("--- [PUT /menu/item/drink/{}] Updating drink item.", itemId);
        DrinkDto updated = menuService.saveDrink(dto);
        return ResponseEntity.ok(updated);
    }

    // -----------------------------------------------------------------
    // --- 7. DELETE: Компонент (Удаление Food или Drink) ---
    // -----------------------------------------------------------------

    /**
     * Удаляет отдельный компонент меню (блюдо или напиток).
     * DELETE /menu/item/{itemId}?isFood=true/false
     */
    @DeleteMapping("/item/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deleteItem(
        @PathVariable Long itemId,
        @RequestParam boolean isFood) {
        
        log.info("--- [DELETE /menu/item/{}] Deleting item (isFood: {}).", itemId, isFood);
        menuService.deleteItem(itemId, isFood);
    }
}
package com.example.com.venom.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.com.venom.dto.Menu.DrinkDto;
import com.example.com.venom.dto.Menu.DrinkOptionDto;
import com.example.com.venom.dto.Menu.DrinksGroupDto;
import com.example.com.venom.dto.Menu.FoodDto;
import com.example.com.venom.dto.Menu.FoodGroupDto;
import com.example.com.venom.dto.Menu.MenuOfEstablishmentDto;
import com.example.com.venom.entity.Menu.DrinkEntity;
import com.example.com.venom.entity.Menu.DrinkOptionEntity;
import com.example.com.venom.entity.Menu.DrinksGroupEntity;
import com.example.com.venom.entity.Menu.FoodEntity;
import com.example.com.venom.entity.Menu.FoodGroupEntity;
import com.example.com.venom.repository.Menu.DrinkOptionRepository;
import com.example.com.venom.repository.Menu.DrinkRepository;
import com.example.com.venom.repository.Menu.DrinksGroupRepository;
import com.example.com.venom.repository.Menu.FoodGroupRepository;
import com.example.com.venom.repository.Menu.FoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

   private final FoodRepository foodRepository;
    private final FoodGroupRepository foodGroupRepository;
    private final DrinksGroupRepository drinksGroupRepository;
    private final DrinkRepository drinkRepository;
    private final DrinkOptionRepository drinkOptionRepository;

    // --- 1. GET (READ) - Агрегация всего меню (Без изменений) ---
    @Transactional(readOnly = true)
    public MenuOfEstablishmentDto getMenuByEstablishmentId(Long establishmentId) {

        // 1. Получаем группы еды
        List<FoodGroupEntity> foodGroupEntities = foodGroupRepository.findByEstablishmentId(establishmentId);
        List<FoodGroupDto> foodGroups = foodGroupEntities.stream()
             .map(this::mapFoodGroupEntityToDto)
             .collect(Collectors.toList());

        // 2. Получаем группы напитков
        List<DrinksGroupEntity> drinkGroupEntities = drinksGroupRepository.findByEstablishmentId(establishmentId);
        List<DrinksGroupDto> drinksGroups = drinkGroupEntities.stream()
             .map(this::mapDrinksGroupEntityToDto)
             .collect(Collectors.toList());

        // 3. Собираем и возвращаем полное меню
        MenuOfEstablishmentDto menuDto = new MenuOfEstablishmentDto();
        menuDto.setEstablishmentId(establishmentId);
        menuDto.setFoodGroups(foodGroups);
        menuDto.setDrinksGroups(drinksGroups);

        return menuDto;
    }

    // --- 2. CREATE/UPDATE Групп ---

    @Transactional
    public FoodGroupDto saveFoodGroup(FoodGroupDto dto) {
        
        boolean isNew = dto.getId() == null || dto.getId() < 1; 
        FoodGroupEntity entity;

        if (isNew) {
            // --- CREATE (POST /menu/group/food) ---
            entity = mapFoodGroupDtoToEntity(dto);
            entity.setId(null); 
            
        } else {
            // --- UPDATE (PUT /menu/group/food/{groupId}) ---
            
            entity = foodGroupRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Группа еды с ID " + dto.getId() + " не найдена для обновления."
                ));
                
            // ⭐ ДОПОЛНЕНИЕ: Обновляем поля
            entity.setName(dto.getName());
            entity.setEstablishmentId(dto.getEstablishmentId()); 
        }

        FoodGroupEntity savedEntity = foodGroupRepository.save(entity);
        
        return mapFoodGroupEntityToDto(savedEntity);
    }

    @Transactional
    public DrinksGroupDto saveDrinksGroup(DrinksGroupDto dto) {
        boolean isNew = dto.getId() == null || dto.getId() < 1; 
        DrinksGroupEntity entity;

        if (isNew) {
            // CREATE: Используем маппер без ID и сбрасываем ID
            entity = mapDrinksGroupDtoToEntity(dto);
            entity.setId(null); 
        } else {
            // UPDATE: Находим существующий ресурс
            entity = drinksGroupRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Группа напитков с ID " + dto.getId() + " не найдена для обновления."
                ));
            // ⭐ ДОПОЛНЕНИЕ: Обновляем поля
            entity.setName(dto.getName());
            entity.setEstablishmentId(dto.getEstablishmentId()); 
        }

        DrinksGroupEntity savedEntity = drinksGroupRepository.save(entity);
        return mapDrinksGroupEntityToDto(savedEntity);
    }

    @Transactional
    public FoodDto saveFood(FoodDto dto) {
        if (dto.getFoodGroupId() == null || !foodGroupRepository.existsById(dto.getFoodGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный ID группы еды.");
        }
        
        boolean isNew = dto.getId() == null || dto.getId() < 1;
        FoodEntity entity;
        
        if (isNew) {
            // CREATE
            entity = mapFoodDtoToEntity(dto);
            entity.setId(null); 
        } else {
            // UPDATE
            entity = foodRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Блюдо с ID " + dto.getId() + " не найдено для обновления."
                ));
            // ⭐ ДОПОЛНЕНИЕ: Обновляем все поля
            entity.setName(dto.getName());
            entity.setCaloriesPer100g(dto.getCaloriesPer100g());
            entity.setFatPer100g(dto.getFatPer100g());
            entity.setCarbohydratesPer100g(dto.getCarbohydratesPer100g());
            entity.setProteinPer100g(dto.getProteinPer100g());
            entity.setIngredients(dto.getIngredients());
            entity.setCost(dto.getCost());
            entity.setWeight(dto.getWeight());
            entity.setPhotoBase64(dto.getPhotoBase64());
            // foodGroupId не обновляем, если блюдо не переносится в другую группу
            // entity.setFoodGroupId(dto.getFoodGroupId()); 
        }

        FoodEntity savedEntity = foodRepository.save(entity);
        return mapFoodEntityToDto(savedEntity);
    }

    @Transactional
    public DrinkDto saveDrink(DrinkDto dto) {
        if (dto.getDrinkGroupId() == null || !drinksGroupRepository.existsById(dto.getDrinkGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный ID группы напитков.");
        }

        boolean isNew = dto.getId() == null || dto.getId() < 1;
        DrinkEntity entity;

        if (isNew) {
            // 1. CREATE Drink
            entity = mapDrinkDtoToEntity(dto);
            entity.setId(null);
        } else {
            // 1. UPDATE Drink
            entity = drinkRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Напиток с ID " + dto.getId() + " не найден для обновления."
                ));
            // ⭐ ДОПОЛНЕНИЕ: Обновляем все поля
            entity.setName(dto.getName());
            entity.setCaloriesPer100g(dto.getCaloriesPer100g());
            entity.setFatPer100g(dto.getFatPer100g());
            entity.setCarbohydratesPer100g(dto.getCarbohydratesPer100g());
            entity.setProteinPer100g(dto.getProteinPer100g());
            entity.setIngredients(dto.getIngredients());
            entity.setPhotoBase64(dto.getPhotoBase64());
            // entity.setDrinkGroupId(dto.getDrinkGroupId()); 
        }

        DrinkEntity savedEntity = drinkRepository.save(entity);
        Long savedDrinkId = savedEntity.getId();

        // 2. Обновляем/сохраняем опции напитка (DrinkOption)
        if (dto.getOptions() != null) {
            
            // Удаляем все старые опции (стратегия "delete and recreate" для опций)
            drinkOptionRepository.deleteAll(drinkOptionRepository.findByDrinkId(savedDrinkId));
            
            List<DrinkOptionEntity> optionEntities = dto.getOptions().stream()
                .map(optionDto -> {
                    DrinkOptionEntity optionEntity = mapDrinkOptionDtoToEntity(optionDto);
                    optionEntity.setDrinkId(savedDrinkId); // Устанавливаем FK
                    optionEntity.setId(null); // Гарантируем, что опции всегда создаются
                    return optionEntity;
                })
                .collect(Collectors.toList());

            drinkOptionRepository.saveAll(optionEntities);
        }

        return mapDrinkEntityToDto(savedEntity);
    }

    // --- 4. DELETE (Удаление) ---

    @Transactional
    public void deleteGroup(Long groupId, boolean isFood) {
        if (isFood) {
            foodRepository.deleteAll(foodRepository.findByFoodGroupId(groupId));
            foodGroupRepository.deleteById(groupId);
        } else {
            List<DrinkEntity> drinks = drinkRepository.findByDrinkGroupId(groupId);
            drinks.forEach(drink -> {
                drinkOptionRepository.deleteAll(drinkOptionRepository.findByDrinkId(drink.getId()));
                drinkRepository.delete(drink);
            });
            drinksGroupRepository.deleteById(groupId);
        }
    }

    @Transactional
    public void deleteItem(Long itemId, boolean isFood) {
        if (isFood) {
            foodRepository.deleteById(itemId);
        } else {
            drinkOptionRepository.deleteAll(drinkOptionRepository.findByDrinkId(itemId));
            drinkRepository.deleteById(itemId);
        }
    }

    // -----------------------------------------------------------------
    // --- 5. МЕТОДЫ МАППИНГА (Мапперы) ---
    // -----------------------------------------------------------------

    // --- Food Group Mappers ---

    private FoodGroupDto mapFoodGroupEntityToDto(FoodGroupEntity entity) {
        FoodGroupDto dto = new FoodGroupDto();
        dto.setId(entity.getId());
        dto.setEstablishmentId(entity.getEstablishmentId());
        dto.setName(entity.getName());

        List<FoodDto> items = foodRepository.findByFoodGroupId(entity.getId()).stream()
            .map(this::mapFoodEntityToDto)
            .collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }

    private FoodGroupEntity mapFoodGroupDtoToEntity(FoodGroupDto dto) {
        FoodGroupEntity entity = new FoodGroupEntity();
        // ID не устанавливается здесь
        entity.setEstablishmentId(dto.getEstablishmentId());
        entity.setName(dto.getName());
        return entity;
    }
    // --- Drinks Group Mappers ---

    private DrinksGroupDto mapDrinksGroupEntityToDto(DrinksGroupEntity entity) {
        DrinksGroupDto dto = new DrinksGroupDto();
        dto.setId(entity.getId());
        dto.setEstablishmentId(entity.getEstablishmentId());
        dto.setName(entity.getName());

        List<DrinkDto> items = drinkRepository.findByDrinkGroupId(entity.getId()).stream()
            .map(this::mapDrinkEntityToDto)
            .collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }


    // --- Food Item Mappers ---

    private FoodDto mapFoodEntityToDto(FoodEntity entity) {
        FoodDto dto = new FoodDto();
        dto.setId(entity.getId());
        dto.setFoodGroupId(entity.getFoodGroupId());
        dto.setName(entity.getName());
        dto.setCaloriesPer100g(entity.getCaloriesPer100g());
        dto.setFatPer100g(entity.getFatPer100g());
        dto.setCarbohydratesPer100g(entity.getCarbohydratesPer100g());
        dto.setProteinPer100g(entity.getProteinPer100g());
        dto.setIngredients(entity.getIngredients());
        dto.setCost(entity.getCost());
        dto.setWeight(entity.getWeight());
        dto.setPhotoBase64(entity.getPhotoBase64());
        return dto;
    }

    // --- Drink Item Mappers ---

    private DrinkDto mapDrinkEntityToDto(DrinkEntity entity) {
        DrinkDto dto = new DrinkDto();
        dto.setId(entity.getId());
        dto.setDrinkGroupId(entity.getDrinkGroupId());
        dto.setName(entity.getName());
        dto.setCaloriesPer100g(entity.getCaloriesPer100g());
        dto.setFatPer100g(entity.getFatPer100g());
        dto.setCarbohydratesPer100g(entity.getCarbohydratesPer100g());
        dto.setProteinPer100g(entity.getProteinPer100g());
        dto.setIngredients(entity.getIngredients());
        dto.setPhotoBase64(entity.getPhotoBase64());

        List<DrinkOptionDto> options = drinkOptionRepository.findByDrinkId(entity.getId()).stream()
            .map(this::mapDrinkOptionEntityToDto)
            .collect(Collectors.toList());
        dto.setOptions(options);

        return dto;
    }

    // --- Drink Option Mappers ---

    private DrinkOptionDto mapDrinkOptionEntityToDto(DrinkOptionEntity entity) {
        DrinkOptionDto dto = new DrinkOptionDto();
        dto.setId(entity.getId());
        dto.setDrinkId(entity.getDrinkId());
        dto.setSizeMl(entity.getSizeMl());
        dto.setCost(entity.getCost());
        return dto;
    }

    private DrinkOptionEntity mapDrinkOptionDtoToEntity(DrinkOptionDto dto) {
        DrinkOptionEntity entity = new DrinkOptionEntity();
        entity.setId(dto.getId());
        entity.setDrinkId(dto.getDrinkId());
        entity.setSizeMl(dto.getSizeMl());
        entity.setCost(dto.getCost());
        return entity;
    }
    

    // --- Drinks Group Mappers ---
    private DrinksGroupEntity mapDrinksGroupDtoToEntity(DrinksGroupDto dto) {
        DrinksGroupEntity entity = new DrinksGroupEntity();
        // ID не устанавливается здесь
        entity.setEstablishmentId(dto.getEstablishmentId());
        entity.setName(dto.getName());
        return entity;
    }

    // --- Food Item Mappers ---
    private FoodEntity mapFoodDtoToEntity(FoodDto dto) {
        FoodEntity entity = new FoodEntity();
        // ID не устанавливается здесь
        entity.setFoodGroupId(dto.getFoodGroupId());
        // ⭐ ДОПОЛНЕНИЕ: остальные поля
        entity.setName(dto.getName());
        entity.setCaloriesPer100g(dto.getCaloriesPer100g());
        entity.setFatPer100g(dto.getFatPer100g());
        entity.setCarbohydratesPer100g(dto.getCarbohydratesPer100g());
        entity.setProteinPer100g(dto.getProteinPer100g());
        entity.setIngredients(dto.getIngredients());
        entity.setCost(dto.getCost());
        entity.setWeight(dto.getWeight());
        entity.setPhotoBase64(dto.getPhotoBase64());
        return entity;
    }

    // --- Drink Item Mappers ---
    private DrinkEntity mapDrinkDtoToEntity(DrinkDto dto) {
        DrinkEntity entity = new DrinkEntity();
        // ID не устанавливается здесь
        entity.setDrinkGroupId(dto.getDrinkGroupId());
        // ⭐ ДОПОЛНЕНИЕ: остальные поля
        entity.setName(dto.getName());
        entity.setCaloriesPer100g(dto.getCaloriesPer100g());
        entity.setFatPer100g(dto.getFatPer100g());
        entity.setCarbohydratesPer100g(dto.getCarbohydratesPer100g());
        entity.setProteinPer100g(dto.getProteinPer100g());
        entity.setIngredients(dto.getIngredients());
        entity.setPhotoBase64(dto.getPhotoBase64());
        return entity;
    }

}
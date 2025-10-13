package com.example.com.venom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.entity.DrinkEntity;
import com.example.com.venom.repository.DrinkRepository;

import java.util.List;

@RestController
@RequestMapping("/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkRepository drinkRepository;

    // Создать новый напиток
    @PostMapping
    public ResponseEntity<DrinkEntity> createDrink(@RequestBody DrinkEntity drink) {
        return ResponseEntity.ok(drinkRepository.save(drink));
    }

    // Получить список всех напитков
    @GetMapping
    public ResponseEntity<List<DrinkEntity>> getAllDrinks() {
        return ResponseEntity.ok(drinkRepository.findAll());
    }

    // Получить напиток по ID
    @GetMapping("/{id}")
    public ResponseEntity<DrinkEntity> getDrinkById(@PathVariable Long id) {
        return drinkRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить напиток
    @PutMapping("/{id}")
    public ResponseEntity<DrinkEntity> updateDrink(
            @PathVariable Long id,
            @RequestBody DrinkEntity drink) {

        return drinkRepository.findById(id)
                .map(existing -> {
                    existing.setName(drink.getName());
                    existing.setPrice(drink.getPrice());
                    existing.setDescription(drink.getDescription());
                    return ResponseEntity.ok(drinkRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить напиток
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrink(@PathVariable Long id) {
        if (drinkRepository.existsById(id)) {
            drinkRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

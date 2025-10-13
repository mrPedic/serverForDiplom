package com.example.com.venom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.entity.DishEntity;
import com.example.com.venom.repository.DishRepository;

import java.util.List;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishRepository dishRepository;

    // Создать новое блюдо
    @PostMapping
    public ResponseEntity<DishEntity> createDish(@RequestBody DishEntity dish) {
        return ResponseEntity.ok(dishRepository.save(dish));
    }

    // Получить список всех блюд
    @GetMapping
    public ResponseEntity<List<DishEntity>> getAllDishes() {
        return ResponseEntity.ok(dishRepository.findAll());
    }

    // Получить блюдо по ID
    @GetMapping("/{id}")
    public ResponseEntity<DishEntity> getDishById(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить блюдо
    @PutMapping("/{id}")
    public ResponseEntity<DishEntity> updateDish(
            @PathVariable Long id,
            @RequestBody DishEntity dish) {

        return dishRepository.findById(id)
                .map(existing -> {
                    existing.setName(dish.getName());
                    existing.setPrice(dish.getPrice());
                    existing.setDescription(dish.getDescription());
                    return ResponseEntity.ok(dishRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить блюдо
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        if (dishRepository.existsById(id)) {
            dishRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

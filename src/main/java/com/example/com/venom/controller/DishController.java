package com.example.com.venom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.entity.DishEntity;
import com.example.com.venom.repository.DishRepository;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishRepository dishRepository;

    @PostMapping
    public ResponseEntity<DishEntity> createDish(@RequestBody DishEntity dish) {
        return ResponseEntity.ok(dishRepository.save(dish));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

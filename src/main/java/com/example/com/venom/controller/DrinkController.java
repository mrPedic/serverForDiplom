package com.example.com.venom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.entity.DrinkEntity;
import com.example.com.venom.repository.DrinkRepository;

@RestController
@RequestMapping("/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkRepository drinkRepository;

    @PostMapping
    public ResponseEntity<DrinkEntity> createDrink(@RequestBody DrinkEntity drink) {
        return ResponseEntity.ok(drinkRepository.save(drink));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrink(@PathVariable Long id) {
        drinkRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

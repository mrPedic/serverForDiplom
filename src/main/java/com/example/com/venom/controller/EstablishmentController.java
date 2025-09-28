package com.example.com.venom.controller;

import com.example.com.venom.dto.MenuDto;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.EstabilishmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstabilishmentRepository estabilishmentRepository;
    private final ObjectMapper objectMapper;

    // ========================== Создание заведения ==========================
    @PostMapping
    public ResponseEntity<?> register(@RequestBody EstablishmentEntity establishmentEntity) {
        Optional<EstablishmentEntity> existing = estabilishmentRepository.findByNameAndAddress(
                establishmentEntity.getName(),
                establishmentEntity.getAddress()
        );
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Заведение с таким названием и адресом уже существует");
        }
        return ResponseEntity.ok(estabilishmentRepository.save(establishmentEntity));
    }

    // ========================== Получение всех заведений ==========================
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(estabilishmentRepository.findAll());
    }

    // ========================== Получение заведения по ID ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return estabilishmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    // ========================== Обновление заведения ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentEntity establishmentEntity) {
        return estabilishmentRepository.findById(id)
                .map(existing -> {
                    establishmentEntity.setId(id);
                    return ResponseEntity.ok(estabilishmentRepository.save(establishmentEntity));
                })
                .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== Удаление заведения ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        return estabilishmentRepository.findById(id)
                .map(existing -> {
                    estabilishmentRepository.delete(existing);
                    return ResponseEntity.ok("Заведение успешно удалено");
                })
                .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== Получение меню заведения ==========================
    @GetMapping("/{id}/menu")
    public ResponseEntity<?> getMenu(@PathVariable Long id) {
        return estabilishmentRepository.findById(id)
                .map(est -> ResponseEntity.ok(est.getMenuJson()))
                .orElse(ResponseEntity.badRequest().body("Меню для заведения не найдено"));
    }

    // ========================== Обновление меню заведения ==========================
    @PutMapping("/{id}/menu")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @RequestBody String newMenuJson) {
        return estabilishmentRepository.findById(id)
                .map(est -> {
                    est.setMenuJson(newMenuJson);
                    estabilishmentRepository.save(est);
                    return ResponseEntity.ok("Меню обновлено");
                })
                .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== Пример изменения меню через ObjectMapper ==========================
    @PutMapping("/{id}/menu/addFoodGroup")
    public ResponseEntity<?> addFoodGroup(@PathVariable Long id, @RequestBody Object foodGroup) throws IOException {
        return estabilishmentRepository.findById(id)
                .map(est -> {
                    try {
                        // 1. Превращаем json в объект
                        MenuDto menu = objectMapper.readValue(est.getMenuJson(), MenuDto.class);

                        // 2. Добавляем новую группу еды
                        menu.getFoodGroups().add(foodGroup);

                        // 3. Сохраняем обратно в JSON
                        est.setMenuJson(objectMapper.writeValueAsString(menu));
                        estabilishmentRepository.save(est);
                        return ResponseEntity.ok("Группа еды добавлена");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Ошибка при обновлении меню: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }
}

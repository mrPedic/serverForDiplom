package com.example.com.venom.controller;

import com.example.com.venom.dto.EstablishmentFavoriteDto;
import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")  // Базовый путь для всех методов
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    // Получение своих данных
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestParam("id") Long id) {
        log.info("--- [CONTROLLER] GET /me: Received id={}", id);
        return userService.getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)  // Если найден → 200 + AccountEntity
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body("Пользователя с таким id не существует")); // 400 + сообщение
    }

    // Обновление данных пользователя
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody UserEntity userEntity) {
        log.info("--- [CONTROLLER] PUT /me: Received id={}", userEntity.getId());
        try {
            userService.updateUser(userEntity);
            return ResponseEntity.ok("Данные успешно обновлены");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Обновление пароля
    @PutMapping("/me/password")
    public ResponseEntity<?> updateMePassword(@RequestBody UserEntity userEntity) {
        log.info("--- [CONTROLLER] PUT /me/password: Received id={}", userEntity.getId());
        try {
            userService.updateUserPassword(userEntity);
            return ResponseEntity.ok("Пароль успешно обновлён");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Удаление пользователя
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteById(@RequestParam("id") Long id) {
        log.info("--- [CONTROLLER] DELETE /me: Received id={}", id);
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok("Пользователь с таким id был удален");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Получить список ID избранных заведений
    @GetMapping("/{userId}/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFavoriteIds(@PathVariable Long userId) {
        log.info("--- [CONTROLLER] GET /{}/favorites: Received userId={}", userId, userId);
        try {
            List<Long> ids = userService.getFavoriteIds(userId);
            return ResponseEntity.ok(ids);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Добавить в избранное
    @PostMapping("/{userId}/favorites/{establishmentId}")
    @Transactional
    public ResponseEntity<?> addFavorite(@PathVariable Long userId, @PathVariable Long establishmentId) {
        log.info("--- [CONTROLLER] POST /{}/favorites/{}: Received", userId, establishmentId);
        try {
            userService.addFavorite(userId, establishmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Удалить из избранного
    @DeleteMapping("/{userId}/favorites/{establishmentId}")
    @Transactional
    public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable Long establishmentId) {
        log.info("--- [CONTROLLER] DELETE /{}/favorites/{}: Received", userId, establishmentId);
        try {
            userService.removeFavorite(userId, establishmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Получить полный список DTO избранных заведений
    @GetMapping("/{userId}/favorites/list")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFavoriteListDto(@PathVariable Long userId) {
        log.info("--- [CONTROLLER] GET /{}/favorites/list: Received userId={}", userId, userId);
        try {
            List<EstablishmentFavoriteDto> dtos = userService.getFavoriteListDto(userId);
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Проверка — находится ли заведение в избранном (для Android)
    @GetMapping("/{userId}/favorites/check/{establishmentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> checkFavorite(@PathVariable Long userId, @PathVariable Long establishmentId) {
        log.info("--- [CONTROLLER] GET /{}/favorites/check/{}: Received", userId, establishmentId);
        try {
            boolean isFavorite = userService.isFavorite(userId, establishmentId);
            return ResponseEntity.ok(isFavorite);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
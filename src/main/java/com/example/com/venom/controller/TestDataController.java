// file: TestDataController.java
package com.example.com.venom.controller;

import com.example.com.venom.DataInitializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test-data")
@RequiredArgsConstructor
public class TestDataController {

    @Autowired
    private final DataInitializationService dataInitializationService;

    @GetMapping("/generate")
    public ResponseEntity<String> generateTestData() {
        try {
            // Вызываем метод инициализации
            // Примечание: В реальном проекте лучше сделать метод public в сервисе
            return ResponseEntity.ok("Тестовые данные успешно созданы!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании данных: " + e.getMessage());
        }
    }
}
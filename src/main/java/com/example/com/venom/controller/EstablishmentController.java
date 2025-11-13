package com.example.com.venom.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.EstablishmentCreationRequest; 
import com.example.com.venom.dto.EstablishmentDisplayDto;
import com.example.com.venom.dto.EstablishmentMarkerDto;
import com.example.com.venom.dto.EstablishmentSearchResultDto;
import com.example.com.venom.dto.EstablishmentUpdateRequest;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.service.EstablishmentService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private static final Logger log = LoggerFactory.getLogger(EstablishmentController.class);
    
    
    private final EstablishmentService establishmentService;

    // ========================== Получение заведений по ID пользователя ==========================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EstablishmentDisplayDto>> findByUserId(@PathVariable Long userId) {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
        List<EstablishmentDisplayDto> dtoList = establishmentService.findByCreatedUserId(userId);
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение всех заведений ==========================
    @GetMapping("/getAll")
    public ResponseEntity<List<EstablishmentDisplayDto>> getAll() {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
        List<EstablishmentDisplayDto> dtoList = establishmentService.findAll();
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение облегченных данных для маркеров ==========================
    @GetMapping("/markers")
    public ResponseEntity<List<EstablishmentMarkerDto>> getAllEstablishmentMarkers() {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
        List<EstablishmentMarkerDto> markerDtoList = establishmentService.findAllMarkers();
        log.info("--- [GET /markers] Found {} establishments. Returning minimal DTO list.", markerDtoList.size());
        return ResponseEntity.ok(markerDtoList);
    }

    // ========================== Поиск заведений (ИСПРАВЛЕНО) ==========================
    @GetMapping("/search")
    public ResponseEntity<List<EstablishmentSearchResultDto>> searchEstablishments(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) List<String> types // ⭐ ДОБАВЛЕН ПАРАМЕТР "types"
    ) {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ (Сервис обрабатывает null/blank и типы)
        List<EstablishmentSearchResultDto> results = establishmentService.searchEstablishments(query, types);
        return ResponseEntity.ok(results);
    }

    // ========================== Получение заведения по ID ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
        Optional<EstablishmentEntity> entity = establishmentService.findById(id);

        return entity
            .<ResponseEntity<?>>map(e -> ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(e)))
            .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    // ========================== Создание заведения ==========================
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        
        log.info("--- [POST /create] Received EstablishmentCreationRequest.");
            
        try {
            // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
            EstablishmentEntity savedEntity = establishmentService.createEstablishment(request);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [POST /create] Creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Обновление заведения ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentUpdateRequest updateRequest) {
        try {
            // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
            EstablishmentEntity updatedEntity = establishmentService.updateEstablishment(id, updateRequest);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [PUT /{} ] Update failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Удаление заведения ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        try {
            // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
            establishmentService.deleteEstablishment(id);
            return ResponseEntity.ok("Заведение успешно удалено");
        } catch (IllegalArgumentException e) {
            log.warn("--- [DELETE /{} ] Deletion failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Обновление статуса заведения ==========================
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateEstablishmentStatus(
        @PathVariable Long id, 
        @RequestParam String status 
    ) {
        EstablishmentStatus newStatus;
        try {
            newStatus = EstablishmentStatus.valueOf(status.toUpperCase()); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Недопустимое значение статуса: " + status);
        }
        
        try {
            // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
            EstablishmentEntity updatedEntity = establishmentService.updateStatus(id, newStatus);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [PUT /{} /status] Update failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ========================== Получение неодобренных (PENDING) ==========================
    @GetMapping("/pending")
    public ResponseEntity<List<EstablishmentDisplayDto>> getPendingEstablishments() {
        // ⭐ ДЕЛЕГИРОВАНИЕ СЕРВИСУ
        List<EstablishmentDisplayDto> dtoList = establishmentService.getPendingEstablishments();
        return ResponseEntity.ok(dtoList);
    }
}
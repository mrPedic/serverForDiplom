package com.example.com.venom.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 

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
import com.example.com.venom.dto.EstablishmentUpdateRequest;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.service.EstablishmentService; // ⭐ ИМПОРТ СЕРВИСА

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private static final Logger log = LoggerFactory.getLogger(EstablishmentController.class);

    private final EstablishmentRepository establishmentRepository;
    private final EstablishmentService establishmentService; // ⭐ ВНЕДРЕНИЕ СЕРВИСА

    // ========================== Получение заведений по ID пользователя ==========================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EstablishmentDisplayDto>> findByUserId(@PathVariable Long userId) {
        List<EstablishmentEntity> userEstablishments = establishmentRepository.findByCreatedUserId(userId);
        
        // МАППИНГ: Преобразуем List<Entity> в List<DisplayDto>
        List<EstablishmentDisplayDto> dtoList = userEstablishments.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
        
        // Возвращаем 200 OK
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение всех заведений ==========================
    @GetMapping("/getAll")
    public ResponseEntity<List<EstablishmentDisplayDto>> getAll() {
        List<EstablishmentEntity> allEntities = establishmentRepository.findAll();
        
        // МАППИНГ: Преобразуем List<Entity> в List<DisplayDto>
        List<EstablishmentDisplayDto> dtoList = allEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение облегченных данных для маркеров ==========================
    /**
     * Возвращает список заведений с минимальным набором полей для отображения на карте.
     */
    @GetMapping("/markers")
    public ResponseEntity<List<EstablishmentMarkerDto>> getAllEstablishmentMarkers() {
        // 1. Загружаем все сущности. Поскольку DTO содержит только простые поля, 
        // это будет быстрее, чем загрузка DisplayDto (который включает большие строки base64, если они не игнорируются).
        List<EstablishmentEntity> allEntities = establishmentRepository.findAll();

        // 2. МАППИНГ: Преобразуем List<Entity> в List<MarkerDto>
        List<EstablishmentMarkerDto> markerDtoList = allEntities.stream()
            .map(EstablishmentMarkerDto::fromEntity) // ⭐ Используем новый маппер
            .collect(Collectors.toList());
        
        log.info("--- [GET /markers] Found {} establishments. Returning minimal DTO list.", markerDtoList.size());

        // 3. Возвращаем 200 OK
        return ResponseEntity.ok(markerDtoList);
    }

    // ========================== Поиск заведений ==========================
    @GetMapping("/search")
    public ResponseEntity<List<EstablishmentDisplayDto>> searchEstablishments(@RequestParam String query) {
        
        // Используем метод репозитория для поиска по названию ИЛИ адресу
        List<EstablishmentEntity> foundEntities = 
            establishmentRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(query, query);
            
        // МАППИНГ
        List<EstablishmentDisplayDto> dtoList = foundEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
            
        // Возвращаем 200 OK
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение заведения по ID (ОБНОВЛЕНО) ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<EstablishmentEntity> entity = establishmentRepository.findById(id);

        // ⭐ ЛОГИРОВАНИЕ: Теперь лог только для строки времени работы
        entity.ifPresent(e -> {
            log.info("--- [GET /{}]: Entity loaded from DB: {}", id, e.toString());
            log.info("--- [GET /{}]: OperatingHours String loaded from DB: {}", id, e.getOperatingHoursString());
        });
        
        return entity
            // МАППИНГ: Преобразуем Optional<Entity> в Optional<DisplayDto> и возвращаем 200 OK
            .<ResponseEntity<?>>map(e -> ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(e)))
            .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    // ========================== Создание заведения (ОБНОВЛЕНО: Используем сервис) ==========================
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        
        log.info("--- [POST /create] Received EstablishmentCreationRequest. OperatingHours String length: {}", 
            request.getOperatingHoursString() != null ? request.getOperatingHoursString().length() : 0);
            
        try {
            // ⭐ ДЕЛЕГИРУЕМ ЛОГИКУ СОЗДАНИЯ СЕРВИСУ
            EstablishmentEntity savedEntity = establishmentService.createEstablishment(request);
            
            // Возвращаем клиенту Display DTO
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
        } catch (IllegalArgumentException e) {
            // Если сработало исключение в сервисе (например, дублирование)
            log.warn("--- [POST /create] Creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // ========================== Обновление заведения (ОБНОВЛЕНО: Используем сервис) ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentUpdateRequest updateRequest) {
        try {
            // ⭐ ДЕЛЕГИРУЕМ ЛОГИКУ ОБНОВЛЕНИЯ СЕРВИСУ
            EstablishmentEntity updatedEntity = establishmentService.updateEstablishment(id, updateRequest);
            
            // Возвращаем Display DTO
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            // Если сработало исключение в сервисе (например, заведение не найдено)
            log.warn("--- [PUT /{} ] Update failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Удаление заведения (ОБНОВЛЕНО: Используем сервис) ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        try {
            // ⭐ ДЕЛЕГИРУЕМ ЛОГИКУ УДАЛЕНИЯ СЕРВИСУ
            establishmentService.deleteEstablishment(id);
            return ResponseEntity.ok("Заведение успешно удалено");
        } catch (IllegalArgumentException e) {
            // Если сработало исключение в сервисе (например, заведение не найдено)
            log.warn("--- [DELETE /{} ] Deletion failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Обновление статуса заведения (ОБНОВЛЕНО: Используем сервис) ==========================
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateEstablishmentStatus(
        @PathVariable Long id, 
        @RequestParam String status 
    ) {
        // 1. Преобразуем строку в ENUM
        EstablishmentStatus newStatus;
        try {
            newStatus = EstablishmentStatus.valueOf(status.toUpperCase()); 
        } catch (IllegalArgumentException e) {
            // 🔥 Если сработал этот блок, сервер вернет 400
            return ResponseEntity.badRequest().body("Недопустимое значение статуса: " + status);
        }
        
        try {
            // ⭐ ДЕЛЕГИРУЕМ ЛОГИКУ ОБНОВЛЕНИЯ СТАТУСА СЕРВИСУ
            EstablishmentEntity updatedEntity = establishmentService.updateStatus(id, newStatus);
            
            // Возвращаем Display DTO
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            // Если сработало исключение в сервисе (например, заведение не найдено)
            log.warn("--- [PUT /{} /status] Update failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ========================== Получение неодобренных (PENDING) ==========================
    @GetMapping("/pending")
    public ResponseEntity<List<EstablishmentDisplayDto>> getPendingEstablishments() {
        // Используем метод репозитория
        List<EstablishmentEntity> pendingEntities = 
            establishmentRepository.findByStatus(EstablishmentStatus.PENDING_APPROVAL);

        List<EstablishmentDisplayDto> dtoList = pendingEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}
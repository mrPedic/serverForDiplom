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
import com.example.com.venom.dto.EstablishmentUpdateRequest;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;

// Удалены неиспользуемые импорты java.util.Map и java.util.stream.Collectors (оставлен один)

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private static final Logger log = LoggerFactory.getLogger(EstablishmentController.class);

    private final EstablishmentRepository establishmentRepository;

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

    // ========================== Создание заведения (КРИТИЧЕСКИ ОБНОВЛЕНО) ==========================
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        
        // Логируем строку времени работы
        log.info("--- [POST /create] Received EstablishmentCreationRequest. OperatingHours String length: {}", 
            request.getOperatingHoursString() != null ? request.getOperatingHoursString().length() : 0);

        String name = request.getName();
        String address = request.getAddress();

        // Проверяем наличие
        Optional<EstablishmentEntity> existing = establishmentRepository.findByNameAndAddress(name, address);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Заведение с таким названием и адресом уже существует");
        }
    
        EstablishmentEntity newEstablishmentEntity = new EstablishmentEntity();
        
        // Устанавливаем все простые поля
        newEstablishmentEntity.setName(request.getName());
        newEstablishmentEntity.setLatitude(request.getLatitude());
        newEstablishmentEntity.setLongitude(request.getLongitude());
        newEstablishmentEntity.setAddress(request.getAddress());
        newEstablishmentEntity.setDescription(request.getDescription());
        newEstablishmentEntity.setCreatedUserId(request.getCreatedUserId());
        newEstablishmentEntity.setType(request.getType());
        newEstablishmentEntity.setPhotoBase64s(request.getPhotoBase64s());
        newEstablishmentEntity.setStatus(EstablishmentStatus.PENDING_APPROVAL); 

        // ⭐ ИСПОЛЬЗУЕМ НОВУЮ СТРОКУ
        newEstablishmentEntity.setOperatingHoursString(request.getOperatingHoursString());

        log.info("--- [POST /create] Entity before save. OperatingHours String: {}", 
            newEstablishmentEntity.getOperatingHoursString());
            
        EstablishmentEntity savedEntity = establishmentRepository.save(newEstablishmentEntity);
        log.info("--- [POST /create] Entity saved successfully with ID: {}", savedEntity.getId());
        
        // Возвращаем клиенту Display DTO
        return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
    }


    // ========================== Обновление заведения (ОБНОВЛЕНО) ==========================
    // Принимает Entity, которая теперь должна включать operatingHoursString
    @PutMapping("/{id}")
    // ⭐ ИСПОЛЬЗУЕМ НОВЫЙ DTO
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentUpdateRequest updateRequest) {
    return establishmentRepository.findById(id)
        .<ResponseEntity<?>>map(existing -> {
            
            // --- 1. Обновляем все поля из DTO ---
            existing.setName(updateRequest.getName());
            existing.setDescription(updateRequest.getDescription());
            existing.setAddress(updateRequest.getAddress());
            existing.setLatitude(updateRequest.getLatitude());
            existing.setLongitude(updateRequest.getLongitude());
            existing.setType(updateRequest.getType());
            existing.setPhotoBase64s(updateRequest.getPhotoBase64s());
            
            // ⭐ 2. ОБЯЗАТЕЛЬНО ОБНОВЛЯЕМ СТРОКУ ВРЕМЕНИ РАБОТЫ
            existing.setOperatingHoursString(updateRequest.getOperatingHoursString());
            
            // (Статус не меняем через PUT, только через отдельный эндпойнт)
            
            // --- 3. Сохраняем изменения ---
            EstablishmentEntity updatedEntity = establishmentRepository.save(existing);
            
            // Возвращаем Display DTO
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        })
        .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
}

    // ========================== Удаление заведения ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        return establishmentRepository.findById(id)
            .map(existing -> {
                establishmentRepository.delete(existing);
                return ResponseEntity.ok("Заведение успешно удалено");
            })
            .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== Одобрение заведения ==========================
    @PutMapping("/{id}/status")
        public ResponseEntity<?> updateEstablishmentStatus(
            @PathVariable Long id, 
            @RequestParam String status 
        ) {
    Optional<EstablishmentEntity> optionalEntity = establishmentRepository.findById(id);

    if (optionalEntity.isEmpty()) {
        return ResponseEntity.badRequest().body("Заведение с таким id не найдено");
    }

    EstablishmentEntity existing = optionalEntity.get();
    
    // --- ВРУЧНУЮ ПРЕОБРАЗУЕМ СТРОКУ В ENUM ---
    EstablishmentStatus newStatus;
    try {
        newStatus = EstablishmentStatus.valueOf(status.toUpperCase()); 
    } catch (IllegalArgumentException e) {
        // 🔥 Если сработал этот блок, сервер вернет 400
        return ResponseEntity.badRequest().body("Недопустимое значение статуса: " + status);
    }
    // -----------------------------------------------------------
    
    // Обновляем статус
    existing.setStatus(newStatus); // Используем преобразованный ENUM
    
    // Сохраняем изменения
    EstablishmentEntity updatedEntity = establishmentRepository.save(existing);
    
    // Возвращаем Display DTO
    return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
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
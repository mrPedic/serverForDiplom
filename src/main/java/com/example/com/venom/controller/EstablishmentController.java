package com.example.com.venom.controller;

import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors; // Для маппинга списка

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
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentRepository establishmentRepository;

    // ⭐ НОВАЯ КОНЕЧНАЯ ТОЧКА: Получение заведений по ID пользователя (ОБНОВЛЕНО)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EstablishmentDisplayDto>> findByUserId(@PathVariable Long userId) {
        List<EstablishmentEntity> userEstablishments = establishmentRepository.findByCreatedUserId(userId);
        
        // МАППИНГ: Преобразуем List<Entity> в List<DisplayDto>
        List<EstablishmentDisplayDto> dtoList = userEstablishments.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
        
        // Возвращаем 200 OK (включая пустой список, если заведений нет)
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение всех заведений (ОБНОВЛЕНО) ==========================
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
        
        // Используем новый метод репозитория для поиска по названию ИЛИ адресу
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
        return establishmentRepository.findById(id)
            // МАППИНГ: Преобразуем Optional<Entity> в Optional<DisplayDto> и возвращаем 200 OK
            .<ResponseEntity<?>>map(entity -> ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(entity)))
            .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        String name = request.getName();
        String address = request.getAddress();

        // Проверяем наличие
        Optional<EstablishmentEntity> existing = establishmentRepository.findByNameAndAddress(name, address);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Заведение с таким названием и адресом уже существует");
        }
    
        // Создаем сущность из DTO
        EstablishmentEntity newEstablishmentEntity = new EstablishmentEntity(
            request.getName(),
            request.getLatitude(),
            request.getLongitude(),
            request.getAddress(),
            request.getDescription(),
            request.getCreatedUserId(),
            request.getType(),
            request.getPhotoBase64s() // ⭐ ПЕРЕДАЕМ НЕОБЯЗАТЕЛЬНЫЙ СПИСОК ФОТО
        );
    
        EstablishmentEntity savedEntity = establishmentRepository.save(newEstablishmentEntity);
        
        // Возвращаем клиенту Display DTO
        return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
    }


    // ========================== Обновление заведения (ОБНОВЛЕНО) ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentEntity establishmentEntity) {
        return establishmentRepository.findById(id)
            .<ResponseEntity<?>>map(existing -> {
                
                // Сохраняем поля, которые не должны меняться (например, дата создания и ID пользователя)
                establishmentEntity.setId(id);
                establishmentEntity.setDateOfCreation(existing.getDateOfCreation());
                establishmentEntity.setCreatedUserId(existing.getCreatedUserId());

                // ⭐ Если входящая сущность НЕ передает фото, мы должны сохранить существующие фото (опционально)
                // Если клиент всегда посылает все данные, это не нужно. 
                // Но если клиент шлет только измененные поля, нужно объединить данные.
                
                // В данном случае, мы доверяем клиенту и просто сохраняем пришедшую сущность.
                // Если photoBase64s = null/"" в запросе, он перезапишет старые данные на null.
                
                EstablishmentEntity updatedEntity = establishmentRepository.save(establishmentEntity);
                
                // Возвращаем Display DTO
                return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
            })
            .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== Удаление заведения (Оставлено без изменений) ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        return establishmentRepository.findById(id)
            .map(existing -> {
                establishmentRepository.delete(existing);
                return ResponseEntity.ok("Заведение успешно удалено");
            })
            .orElse(ResponseEntity.badRequest().body("Заведение с таким id не найдено"));
    }

    // ========================== ⭐ НОВАЯ КОНЕЧНАЯ ТОЧКА: Одобрение заведения ==========================
    // Принимает ID заведения и новый статус в теле запроса (или в параметре).
    // Для простоты будем использовать PUT, который меняет только статус.
    @PutMapping("/{id}/status")
        public ResponseEntity<?> updateEstablishmentStatus(
            @PathVariable Long id, 
            @RequestParam String status // <-- Изменили тип на String
        ) {
    Optional<EstablishmentEntity> optionalEntity = establishmentRepository.findById(id);

    if (optionalEntity.isEmpty()) {
        return ResponseEntity.badRequest().body("Заведение с таким id не найдено");
    }

    EstablishmentEntity existing = optionalEntity.get();
    
    // --- ⭐ ВАЖНОЕ ИЗМЕНЕНИЕ: ВРУЧНУЮ ПРЕОБРАЗУЕМ СТРОКУ В ENUM ---
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
    
    // ========================== ⭐ НОВАЯ КОНЕЧНАЯ ТОЧКА: Получение неодобренных (PENDING) ==========================
    @GetMapping("/pending")
    public ResponseEntity<List<EstablishmentDisplayDto>> getPendingEstablishments() {
        // Используем новый метод репозитория
        List<EstablishmentEntity> pendingEntities = 
            establishmentRepository.findByStatus(EstablishmentStatus.PENDING_APPROVAL);

        List<EstablishmentDisplayDto> dtoList = pendingEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}
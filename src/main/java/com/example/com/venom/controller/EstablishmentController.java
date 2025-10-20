package com.example.com.venom.controller;

import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors; // Для маппинга списка

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.dto.EstablishmentCreationRequest;
import com.example.com.venom.dto.EstablishmentDisplayDto; 
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentRepository establishmentRepository;

    
    // ========================== Создание заведения (ОБНОВЛЕНО) ==========================
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        String name = request.getName();
        String address = request.getAddress();

        // Проверяем наличие
        Optional<EstablishmentEntity> existing = establishmentRepository.findByNameAndAddress(name, address);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Заведение с таким названием и адресом уже существует");
        }
    
        // Создаем сущность из DTO (ОБНОВЛЕННЫЙ КОНСТРУКТОР)
        EstablishmentEntity newEstablishmentEntity = new EstablishmentEntity(
            request.getName(),
            request.getLatitude(),
            request.getLongitude(),
            request.getAddress(),
            request.getDescription(),
            request.getCreatedUserId(),
            request.getType() // ⭐ ПЕРЕДАЕМ ТИП ИЗ ЗАПРОСА
        );
    
        EstablishmentEntity savedEntity = establishmentRepository.save(newEstablishmentEntity);
        
        // Возвращаем клиенту Display DTO
        return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
    }

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

    // ========================== Получение заведения по ID (ОБНОВЛЕНО) ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return establishmentRepository.findById(id)
            // МАППИНГ: Преобразуем Optional<Entity> в Optional<DisplayDto> и возвращаем 200 OK
            .<ResponseEntity<?>>map(entity -> ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(entity)))
            .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    // ========================== Обновление заведения (ОБНОВЛЕНО) ==========================
    // Принимаем EstablishmentEntity для простоты PUT, но можно использовать отдельный DTO
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentEntity establishmentEntity) {
        return establishmentRepository.findById(id)
            .<ResponseEntity<?>>map(existing -> {
                // Убеждаемся, что ID в сущности установлен
                establishmentEntity.setId(id);
                // (Дополнительная логика для сохранения полей, которые не должны обновляться, опущена)
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
}
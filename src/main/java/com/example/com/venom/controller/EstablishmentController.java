package com.example.com.venom.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.EstablishmentCreationRequest;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentRepository establishmentRepository;

    // ========================== Создание заведения (ИСПРАВЛЕНО) ==========================
    // Теперь принимаем EstablishmentCreationRequest DTO вместо Map<String, String>.
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
        // Используем DTO для передачи всех необходимых полей
        EstablishmentEntity newEstablishmentEntity = new EstablishmentEntity(
                name,
                request.getLatitude(),
                request.getLongitude(),
                address,
                request.getDescription(),
                request.getCreatedUserId()
        );
        
        // Устанавливаем дату, которую должен генерировать сервер, если она не пришла в запросе, 
        // но так как клиент ее присылает, просто полагаемся на DTO, либо переопределяем на сервере:
        // newEstablishmentEntity.setDateOfCreation(LocalDate.now());

        return ResponseEntity.ok(establishmentRepository.save(newEstablishmentEntity));
    }

    // ========================== Получение всех заведений ==========================
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(establishmentRepository.findAll());
    }

    // ========================== Получение заведения по ID ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return establishmentRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Заведения с таким id не существует"));
    }

    // ========================== Обновление заведения ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody EstablishmentEntity establishmentEntity) {
        return establishmentRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    // Убеждаемся, что ID в сущности установлен
                    establishmentEntity.setId(id);
                    // Здесь может потребоваться дополнительная логика для сохранения полей, 
                    // которые не должны обновляться (например, createdUserId, dateOfCreation)
                    return ResponseEntity.ok(establishmentRepository.save(establishmentEntity));
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
}

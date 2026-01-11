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

import com.example.com.venom.dto.establishment.EstablishmentCreationRequest;
import com.example.com.venom.dto.establishment.EstablishmentDisplayDto;
import com.example.com.venom.dto.establishment.EstablishmentMarkerDto;
import com.example.com.venom.dto.establishment.EstablishmentSearchResultDto;
import com.example.com.venom.dto.establishment.EstablishmentUpdateRequest;
import com.example.com.venom.dto.forEstablishmentDetailScreen.DescriptionDTO;
import com.example.com.venom.dto.forEstablishmentDetailScreen.MapDTO;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentStatus;
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
        log.info("--- [CONTROLLER] GET /user/{}: Received userId={}", userId, userId);
        List<EstablishmentDisplayDto> dtoList = establishmentService.findByCreatedUserId(userId);
        log.info("--- [CONTROLLER] GET /user/{}: Returning {} establishments", userId, dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение всех заведений ==========================
    @GetMapping("/getAll")
    public ResponseEntity<List<EstablishmentDisplayDto>> getAll() {
        log.info("--- [CONTROLLER] GET /getAll: Received request");
        List<EstablishmentDisplayDto> dtoList = establishmentService.findAll();
        log.info("--- [CONTROLLER] GET /getAll: Returning {} establishments", dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    // ========================== Получение облегченных данных для маркеров ==========================
    @GetMapping("/markers")
    public ResponseEntity<List<EstablishmentMarkerDto>> getAllEstablishmentMarkers() {
        log.info("--- [CONTROLLER] GET /markers: Received request");
        List<EstablishmentMarkerDto> markerDtoList = establishmentService.findAllMarkers();
        log.info("--- [CONTROLLER] GET /markers: Returning {} markers", markerDtoList.size());
        return ResponseEntity.ok(markerDtoList);
    }

    // ========================== Поиск заведений (ИСПРАВЛЕНО) ==========================
    @GetMapping("/search")
    public ResponseEntity<List<EstablishmentSearchResultDto>> searchEstablishments(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> types // ⭐ ДОБАВЛЕН ПАРАМЕТР "types"
    ) {
        log.info("--- [CONTROLLER] GET /search: Received query='{}', types={}", query, types);
        List<EstablishmentSearchResultDto> results = establishmentService.searchEstablishments(query, types);
        log.info("--- [CONTROLLER] GET /search: Returning {} results", results.size());
        return ResponseEntity.ok(results);
    }

    // ========================== Получение заведения по ID ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        log.info("--- [CONTROLLER] GET /{id}: Received id={}", id);
        Optional<EstablishmentEntity> entity = establishmentService.findById(id);
        if (entity.isPresent()) {
            log.info("--- [CONTROLLER] GET /{id}: Found and returning DTO for id={}", id);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(entity.get()));
        } else {
            log.warn("--- [CONTROLLER] GET /{id}: Not found for id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ========================== Создание заведения ==========================
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody EstablishmentCreationRequest request) {
        log.info("--- [CONTROLLER] POST /create: Received request with name='{}', address='{}'", request.getName(), request.getAddress());
        try {
            EstablishmentEntity savedEntity = establishmentService.createEstablishment(request);
            log.info("--- [CONTROLLER] POST /create: Created and returning DTO for id={}", savedEntity.getId());
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(savedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] POST /create: Failed - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Получение фото заведения по ID ==========================
    @GetMapping("/{id}/photos")
    public ResponseEntity<List<String>> getPhotos(@PathVariable Long id) {
        log.info("--- [CONTROLLER] GET /{id}/photos: Received id={}", id);
        Optional<EstablishmentEntity> entity = establishmentService.findById(id);
        if (entity.isPresent()) {
            log.info("--- [CONTROLLER] GET /{id}/photos: Found and returning {} photos for id={}", entity.get().getPhotoBase64s().size(), id);
            return ResponseEntity.ok(entity.get().getPhotoBase64s());
        } else {
            log.warn("--- [CONTROLLER] GET /{id}/photos: Not found for id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ========================== Обновление заведения ==========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEstablishment(
            @PathVariable Long id,
            @RequestBody EstablishmentUpdateRequest updateRequest) {
        log.info("--- [CONTROLLER] PUT /{id}: Received id={}, updateRequest with name='{}'", id, updateRequest.getName());
        try {
            EstablishmentEntity updatedEntity = establishmentService.updateEstablishment(id, updateRequest);
            log.info("--- [CONTROLLER] PUT /{id}: Updated and returning DTO for id={}", id);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] PUT /{id}: Failed - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Удаление заведения ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        log.info("--- [CONTROLLER] DELETE /{id}: Received id={}", id);
        try {
            establishmentService.deleteEstablishment(id);
            log.info("--- [CONTROLLER] DELETE /{id}: Deleted successfully for id={}", id);
            return ResponseEntity.ok("Заведение успешно удалено");
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] DELETE /{id}: Failed - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Обновление статуса заведения ==========================
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateEstablishmentStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("--- [CONTROLLER] PUT /{id}/status: Received id={}, status={}", id, status);
        EstablishmentStatus newStatus;
        try {
            newStatus = EstablishmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] PUT /{id}/status: Invalid status value: {}", status);
            return ResponseEntity.badRequest().body("Недопустимое значение статуса: " + status);
        }

        try {
            EstablishmentEntity updatedEntity = establishmentService.updateStatus(id, newStatus);
            log.info("--- [CONTROLLER] PUT /{id}/status: Updated and returning DTO for id={}", id);
            return ResponseEntity.ok(EstablishmentDisplayDto.fromEntity(updatedEntity));
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] PUT /{id}/status: Failed - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========================== Получение неодобренных (PENDING) ==========================
    @GetMapping("/pending")
    public ResponseEntity<List<EstablishmentDisplayDto>> getPendingEstablishments() {
        log.info("--- [CONTROLLER] GET /pending: Received request");
        List<EstablishmentDisplayDto> dtoList = establishmentService.getPendingEstablishments();
        log.info("--- [CONTROLLER] GET /pending: Returning {} pending establishments", dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    // Новые эндпоинты для разнопотоковой загрузки

    @GetMapping("/{id}/description")
    public ResponseEntity<DescriptionDTO> getDescription(@PathVariable Long id) {
        log.info("--- [CONTROLLER] GET /{id}/description: Received id={}", id);
        try {
            DescriptionDTO dto = establishmentService.getDescription(id);
            log.info("--- [CONTROLLER] GET /{id}/description: Returning DTO for id={}", id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] GET /{id}/description: Not found for id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/map")
    public ResponseEntity<MapDTO> getMapData(@PathVariable Long id) {
        log.info("--- [CONTROLLER] GET /{id}/map: Received id={}", id);
        try {
            MapDTO dto = establishmentService.getMapData(id);
            log.info("--- [CONTROLLER] GET /{id}/map: Returning DTO for id={}", id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("--- [CONTROLLER] GET /{id}/map: Not found for id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}

package com.example.com.venom.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.com.venom.dto.EstablishmentCreationRequest;
import com.example.com.venom.dto.EstablishmentDisplayDto;
import com.example.com.venom.dto.EstablishmentMarkerDto;
import com.example.com.venom.dto.EstablishmentSearchResultDto;
import com.example.com.venom.dto.EstablishmentUpdateRequest;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;

    // ========================== МЕТОДЫ ПОЛУЧЕНИЯ ДАННЫХ ==========================

    /**
     * Получает все заведения для отображения.
     * @return Список DTO заведений.
     */
    public List<EstablishmentDisplayDto> findAll() {
        List<EstablishmentEntity> allEntities = establishmentRepository.findAll();
        return allEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Возвращает список заведений с минимальным набором полей для отображения на карте.
     * @return Список Marker DTO.
     */
    public List<EstablishmentMarkerDto> findAllMarkers() {
        List<EstablishmentEntity> allEntities = establishmentRepository.findAll();
        List<EstablishmentMarkerDto> markerDtoList = allEntities.stream()
            .map(EstablishmentMarkerDto::fromEntity)
            .collect(Collectors.toList());
        log.info("--- [Service: Markers] Found {} establishments.", markerDtoList.size());
        return markerDtoList;
    }

    /**
     * Получает заведение по ID.
     * @param id ID заведения.
     * @return Optional с сущностью заведения.
     */
    public Optional<EstablishmentEntity> findById(Long id) {
        Optional<EstablishmentEntity> entity = establishmentRepository.findById(id);
        entity.ifPresent(e -> log.info("--- [Service: FindById] Entity ID {} loaded. OperatingHours: {}", id, e.getOperatingHoursString()));
        return entity;
    }

    /**
     * Получает все заведения, созданные определенным пользователем.
     * @param userId ID пользователя.
     * @return Список DTO заведений.
     */
    public List<EstablishmentDisplayDto> findByCreatedUserId(Long userId) {
        List<EstablishmentEntity> userEstablishments = establishmentRepository.findByCreatedUserId(userId);
        return userEstablishments.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Получает неодобренные заведения.
     * @return Список DTO неодобренных заведений.
     */
    public List<EstablishmentDisplayDto> getPendingEstablishments() {
        List<EstablishmentEntity> pendingEntities =
        establishmentRepository.findByStatus(EstablishmentStatus.PENDING_APPROVAL);
        return pendingEntities.stream()
            .map(EstablishmentDisplayDto::fromEntity)
            .collect(Collectors.toList());
    }


    // ========================== МЕТОДЫ ПОИСКА ==========================

    /**
     * Выполняет поиск заведений по названию или адресу и маппит их в облегченный DTO.
     * @param query Строка поиска.
     * @return Список EstablishmentSearchResultDto.
     */
    public List<EstablishmentSearchResultDto> searchEstablishments(String query) {

        log.info("--- [Service: Search] Executing repository search with query: '{}'", query);

        if (query == null || query.isBlank()) {
            log.warn("--- [Service: Search] Received blank or null query. Returning empty list.");
            return Collections.emptyList();
        }

        // ⭐ ДОБАВЛЕННЫЙ ЛОГ: Явно показываем строку, которая идет в репозиторий
        log.info("--- [Service: Search] Executing repository search with query: '{}'", query);

        // ИСПОЛЬЗУЕМ ИСПРАВЛЕННЫЙ МЕТОД РЕПОЗИТОРИЯ (searchByNameOrAddress)
        List<EstablishmentEntity> foundEntities =
        establishmentRepository.searchByNameOrAddress(query);

        List<EstablishmentSearchResultDto> dtoList = foundEntities.stream()
            .map(EstablishmentSearchResultDto::fromEntity)
            .collect(Collectors.toList());

        log.info("--- [Service: Search] Found {} results for query '{}'.", dtoList.size(), query);

        return dtoList;
    }

    // ========================== МЕТОДЫ ИЗМЕНЕНИЯ (CRUD) ==========================

      /**
      * Создает новое заведение после проверки на дублирование.
      * @param request DTO с данными для создания.
      * @return EstablishmentEntity.
      * @throws IllegalArgumentException если заведение с таким названием и адресом уже существует.
      */
    public EstablishmentEntity createEstablishment(EstablishmentCreationRequest request) {
        log.info("--- [Service: Create] Received request. Name: {}, Address: {}", request.getName(), request.getAddress());

        // 1. Проверка на дублирование
        Optional<EstablishmentEntity> existing = establishmentRepository.findByNameAndAddress(
                request.getName(),
        request.getAddress()
        );

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Заведение с таким названием и адресом уже существует.");
        }

        // 2. Маппинг DTO в Entity
        EstablishmentEntity newEstablishmentEntity = new EstablishmentEntity();

        newEstablishmentEntity.setName(request.getName());
        newEstablishmentEntity.setLatitude(request.getLatitude());
        newEstablishmentEntity.setLongitude(request.getLongitude());
        newEstablishmentEntity.setAddress(request.getAddress());
        newEstablishmentEntity.setDescription(request.getDescription());
        newEstablishmentEntity.setCreatedUserId(request.getCreatedUserId());
        newEstablishmentEntity.setType(request.getType());
        newEstablishmentEntity.setPhotoBase64s(request.getPhotoBase64s());
        newEstablishmentEntity.setOperatingHoursString(request.getOperatingHoursString());
        newEstablishmentEntity.setStatus(EstablishmentStatus.PENDING_APPROVAL);
        newEstablishmentEntity.setRating(0.0); // Устанавливаем начальный рейтинг

        // 3. Сохранение
        EstablishmentEntity savedEntity = establishmentRepository.save(newEstablishmentEntity);
        log.info("--- [Service: Create] Entity saved successfully with ID: {}", savedEntity.getId());

        return savedEntity;
    }

      /**
      * Обновляет существующее заведение по ID.
      * @param id ID заведения.
      * @param updateRequest DTO с данными для обновления.
      * @return Обновленный EstablishmentEntity.
      * @throws IllegalArgumentException если заведение не найдено.
      */
    public EstablishmentEntity updateEstablishment(Long id, EstablishmentUpdateRequest updateRequest) {
        log.info("--- [Service: Update] Starting update for ID: {}", id);

        // 1. Поиск существующей сущности
        EstablishmentEntity existing = establishmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Заведение с id " + id + " не найдено."));

        // 2. Обновление полей
        existing.setName(updateRequest.getName());
        existing.setDescription(updateRequest.getDescription());
        existing.setAddress(updateRequest.getAddress());
        existing.setLatitude(updateRequest.getLatitude());
        existing.setLongitude(updateRequest.getLongitude());
        existing.setType(updateRequest.getType());
        existing.setPhotoBase64s(updateRequest.getPhotoBase64s());
        existing.setOperatingHoursString(updateRequest.getOperatingHoursString());

        // 3. Сохранение
        EstablishmentEntity updatedEntity = establishmentRepository.save(existing);
        log.info("--- [Service: Update] Entity ID {} updated successfully.", id);

        return updatedEntity;
    }

      /**
      * Обновляет статус заведения.
      * @param id ID заведения.
      * @param newStatus Новый статус.
     * @return Обновленный EstablishmentEntity.
      * @throws IllegalArgumentException если заведение не найдено.
      */
    public EstablishmentEntity updateStatus(Long id, EstablishmentStatus newStatus) {
        EstablishmentEntity existing = establishmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Заведение с id " + id + " не найдено."));

        existing.setStatus(newStatus);

        EstablishmentEntity updatedEntity = establishmentRepository.save(existing);
        log.info("--- [Service: Update Status] Entity ID {} status updated to {}.", id, newStatus);
        return updatedEntity;
    }

      /**
      * Удаляет заведение по ID.
      * @param id ID заведения.
      * @throws IllegalArgumentException если заведение не найдено.
      */
    public void deleteEstablishment(Long id) {
        if (!establishmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Заведение с id " + id + " не найдено.");
        }
        establishmentRepository.deleteById(id);
        log.info("--- [Service: Delete] Entity ID {} deleted successfully.", id);
    }
}
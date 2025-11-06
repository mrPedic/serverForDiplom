package com.example.com.venom.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.com.venom.dto.EstablishmentCreationRequest;
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
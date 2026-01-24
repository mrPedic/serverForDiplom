package com.example.com.venom.service;

import com.example.com.venom.dto.establishment.*;
import com.example.com.venom.dto.forEstablishmentDetailScreen.DescriptionDTO;
import com.example.com.venom.dto.forEstablishmentDetailScreen.MapDTO;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.BookingStatus;
import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;
import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.repository.BookingRepository;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstablishmentService {

    private static final Logger log = LoggerFactory.getLogger(EstablishmentService.class);

    private final EstablishmentRepository repository;
    private final OrderRepository orderRepository;
    private final BookingRepository bookingRepository;

    public List<EstablishmentDisplayDto> findByCreatedUserId(Long userId) {
        log.info("--- [SERVICE] findByCreatedUserId: Received userId={}", userId);
        List<EstablishmentEntity> entities = repository.findByCreatedUserId(userId);
        List<EstablishmentDisplayDto> dtos = entities.stream()
                .map(EstablishmentDisplayDto::fromEntity)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] findByCreatedUserId: Found {} establishments", dtos.size());
        return dtos;
    }

    public List<EstablishmentDisplayDto> findAll() {
        log.info("--- [SERVICE] findAll: Received request");
        List<EstablishmentEntity> entities = repository.findAll();
        List<EstablishmentDisplayDto> dtos = entities.stream()
                .map(EstablishmentDisplayDto::fromEntity)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] findAll: Found {} establishments", dtos.size());
        return dtos;
    }

    public List<EstablishmentMarkerDto> findAllMarkers() {
        log.info("--- [SERVICE] findAllMarkers: Received request");
        List<EstablishmentEntity> entities = repository.findAll();
        List<EstablishmentMarkerDto> markers = entities.stream()
                .map(EstablishmentMarkerDto::fromEntity)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] findAllMarkers: Found {} markers", markers.size());
        return markers;
    }

    public List<EstablishmentSearchResultDto> searchEstablishments(String query, List<String> types) {
        log.info("--- [SERVICE] searchEstablishments: Received query='{}', types={}", query, types);
        List<EstablishmentType> parsedTypes = (types != null && !types.isEmpty())
                ? types.stream().map(EstablishmentType::valueOf).collect(Collectors.toList())
                : null;

        List<EstablishmentEntity> entities;
        if (query == null || query.isBlank()) {
            if (parsedTypes != null && !parsedTypes.isEmpty()) {
                entities = repository.findByTypeIn(parsedTypes);
            } else {
                entities = repository.findAll();
            }
        } else {
            if (parsedTypes != null && !parsedTypes.isEmpty()) {
                entities = repository.searchByNameOrAddressAndType(query, parsedTypes);
            } else {
                entities = repository.searchByNameOrAddress(query);
            }
        }

        List<EstablishmentSearchResultDto> results = entities.stream()
                .map(EstablishmentSearchResultDto::fromEntity)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] searchEstablishments: Found {} results", results.size());
        return results;
    }

    public Optional<EstablishmentEntity> findById(Long id) {
        log.info("--- [SERVICE] findById: Received id={}", id);
        Optional<EstablishmentEntity> entity = repository.findById(id);
        log.info("--- [SERVICE] findById: Found entity? {}", entity.isPresent());
        return entity;
    }

    public EstablishmentEntity createEstablishment(EstablishmentCreationRequest request) {
        log.info("--- [SERVICE] createEstablishment: Received request with name='{}', address='{}'", request.getName(), request.getAddress());
        if (repository.findByNameAndAddress(request.getName(), request.getAddress()).isPresent()) {
            log.warn("--- [SERVICE] createEstablishment: Duplicate found");
            throw new IllegalArgumentException("Заведение с таким именем и адресом уже существует");
        }
        EstablishmentEntity entity = new EstablishmentEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setAddress(request.getAddress());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());
        entity.setType(request.getType());
        entity.setCreatedUserId(request.getCreatedUserId());
        entity.setStatus(EstablishmentStatus.PENDING_APPROVAL);
        entity.setPhotoBase64s(request.getPhotoBase64s());
        entity.setOperatingHoursString(request.getOperatingHoursString());
        EstablishmentEntity saved = repository.save(entity);
        log.info("--- [SERVICE] createEstablishment: Saved new establishment with id={}", saved.getId());
        return saved;
    }

    public EstablishmentEntity updateEstablishment(Long id, EstablishmentUpdateRequest updateRequest) {
        log.info("--- [SERVICE] updateEstablishment: Received id={}, updateRequest with name='{}'", id, updateRequest.getName());
        EstablishmentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));
        entity.setName(updateRequest.getName());
        entity.setDescription(updateRequest.getDescription());
        entity.setAddress(updateRequest.getAddress());
        entity.setLatitude(updateRequest.getLatitude());
        entity.setLongitude(updateRequest.getLongitude());
        entity.setType(updateRequest.getType());
        entity.setPhotoBase64s(updateRequest.getPhotoBase64s());
        entity.setOperatingHoursString(updateRequest.getOperatingHoursString());
        EstablishmentEntity updated = repository.save(entity);
        log.info("--- [SERVICE] updateEstablishment: Updated establishment with id={}", updated.getId());
        return updated;
    }

    public void deleteEstablishment(Long id) {
        log.info("--- [SERVICE] deleteEstablishment: Received id={}", id);
        if (!repository.existsById(id)) {
            log.warn("--- [SERVICE] deleteEstablishment: Not found");
            throw new IllegalArgumentException("Заведение не найдено");
        }
        repository.deleteById(id);
        log.info("--- [SERVICE] deleteEstablishment: Deleted establishment with id={}", id);
    }

    public EstablishmentEntity updateStatus(Long id, EstablishmentStatus newStatus) {
        log.info("--- [SERVICE] updateStatus: Received id={}, newStatus={}", id, newStatus);
        EstablishmentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));
        entity.setStatus(newStatus);
        EstablishmentEntity updated = repository.save(entity);
        log.info("--- [SERVICE] updateStatus: Updated status to {} for id={}", updated.getStatus(), id);
        return updated;
    }

    public List<EstablishmentDisplayDto> getPendingEstablishments() {
        log.info("--- [SERVICE] getPendingEstablishments: Received request");
        List<EstablishmentEntity> entities = repository.findByStatus(EstablishmentStatus.PENDING_APPROVAL);
        List<EstablishmentDisplayDto> dtos = entities.stream()
                .map(EstablishmentDisplayDto::fromEntity)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] getPendingEstablishments: Found {} pending establishments", dtos.size());
        return dtos;
    }

    // Новые методы для разнопотоковой загрузки

    public DescriptionDTO getDescription(Long id) {
        log.info("--- [SERVICE] getDescription: Received id={}", id);
        EstablishmentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));
        DescriptionDTO dto = new DescriptionDTO(
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getRating(),
                entity.getType(),
                entity.getOperatingHoursString(),
                entity.getDateOfCreation()
        );
        log.info("--- [SERVICE] getDescription: Returning DTO for id={}", id);
        return dto;
    }

    public MapDTO getMapData(Long id) {
        log.info("--- [SERVICE] getMapData: Received id={}", id);
        EstablishmentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));
        MapDTO dto = new MapDTO(
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAddress()
        );
        log.info("--- [SERVICE] getMapData: Returning DTO for id={}", id);
        return dto;
    }

    /**
     * Получить заведения пользователя с количеством ожидающих заказов и бронирований
     */
    public List<EstablishmentWithCountsDto> getEstablishmentsWithCountsByUserId(Long userId) {
        log.info("--- [SERVICE] getEstablishmentsWithCountsByUserId: Received userId={}", userId);

        List<EstablishmentEntity> entities = repository.findByCreatedUserId(userId);
        List<EstablishmentWithCountsDto> dtos = entities.stream()
                .map(entity -> {
                    // Подсчитываем количество pending заказов и бронирований
                    int pendingOrdersCount = orderRepository.countByEstablishmentIdAndStatus(
                            entity.getId(), OrderStatus.PENDING);
                    int pendingBookingsCount = bookingRepository.countByEstablishmentIdAndStatus(
                            entity.getId(), BookingStatus.PENDING);

                    return EstablishmentWithCountsDto.fromEntity(
                            entity, pendingOrdersCount, pendingBookingsCount);
                })
                .collect(Collectors.toList());

        log.info("--- [SERVICE] getEstablishmentsWithCountsByUserId: Found {} establishments with counts", dtos.size());
        return dtos;
    }

    /**
     * Получить количество pending заказов для заведения
     */
    public int getPendingOrderCount(Long establishmentId) {
        return orderRepository.countByEstablishmentIdAndStatus(establishmentId, OrderStatus.PENDING);
    }

    /**
     * Получить количество pending бронирований для заведения
     */
    public int getPendingBookingCount(Long establishmentId) {
        return bookingRepository.countByEstablishmentIdAndStatus(establishmentId, BookingStatus.PENDING);
    }
}
package com.example.com.venom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.com.venom.dto.booking.OwnerBookingDisplayDto;
import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.com.venom.enums.BookingStatus;
import com.example.com.venom.dto.booking.BookingCreationDto;
import com.example.com.venom.dto.booking.BookingDisplayDto;
import com.example.com.venom.entity.BookingEntity;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.repository.BookingRepository;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.TableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository;
    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WebSocketNotificationService webSocketNotificationService; // 🔥 ИСПОЛЬЗУЕМ Kotlin-сервис

    @Transactional
    public BookingEntity createBooking(BookingCreationDto dto) {
        TableEntity table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Столик не найден или не принадлежит заведению."));

        if (!table.getEstablishmentId().equals(dto.getEstablishmentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Столик не принадлежит этому заведению.");
        }

        LocalDateTime endTime = dto.getStartTime().plusMinutes(dto.getDurationMinutes());

        // Проверка пересечения с активными бронями
        List<Long> reservedIds = bookingRepository.findReservedTableIds(
                dto.getEstablishmentId(),
                dto.getStartTime(),
                endTime
        );

        if (reservedIds.contains(dto.getTableId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Столик уже забронирован на это время.");
        }

        BookingEntity booking = new BookingEntity();
        booking.setUserId(dto.getUserId());
        booking.setEstablishmentId(dto.getEstablishmentId());
        booking.setTableId(dto.getTableId());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(endTime);
        booking.setNumPeople(dto.getNumPeople());
        booking.setNotes(dto.getNotes());
        booking.setGuestPhone(dto.getGuestPhone());
        booking.setStatus(BookingStatus.PENDING);

        BookingEntity savedBooking = bookingRepository.save(booking);

        // 🔥 ОТПРАВЛЯЕМ УВЕДОМЛЕНИЕ ЧЕРЕЗ WEBSOCKET
        sendBookingNotification(savedBooking, table);

        return savedBooking;
    }

    // 🔥 НОВЫЙ МЕТОД: Отправка уведомления о новой брони
    private void sendBookingNotification(BookingEntity booking, TableEntity table) {
        try {
            EstablishmentEntity establishment = establishmentRepository.findById(booking.getEstablishmentId())
                    .orElse(null);

            log.info("🔍 Finding owner for establishment: {}", booking.getEstablishmentId());

            Long ownerId = null;
            if (establishment != null) {
                ownerId = establishment.getCreatedUserId();
                log.info("🔍 Found owner ID: {} for establishment: {}",
                        ownerId, establishment.getName());
            } else {
                log.warn("⚠️ Establishment not found: {}", booking.getEstablishmentId());
            }

            UserEntity user = userRepository.findById(booking.getUserId())
                    .orElse(null);

            String establishmentName = "Неизвестное заведение";
            String ownerName = "Неизвестный владелец";

            if (establishment != null) {
                ownerId = establishment.getCreatedUserId();
                establishmentName = establishment.getName();

                // Получаем имя владельца
                UserEntity owner = userRepository.findById(ownerId).orElse(null);
                if (owner != null) {
                    ownerName = owner.getName();
                }
            }

            String userName = user != null ? user.getName() : "Гость";
            String tableName = table != null ? table.getName() : "Неизвестный стол";

            // Формируем JSON уведомления с помощью Jackson ObjectMapper
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("type", "NEW_BOOKING");

            ObjectNode data = objectMapper.createObjectNode();
            data.put("bookingId", booking.getId());
            data.put("establishmentId", booking.getEstablishmentId());
            data.put("establishmentName", establishmentName);
            data.put("ownerId", ownerId != null ? ownerId : 0);
            data.put("ownerName", ownerName);
            data.put("userName", userName);
            data.put("userPhone", booking.getGuestPhone() != null ? booking.getGuestPhone() : "");
            data.put("startTime", booking.getStartTime().toString());
            data.put("numPeople", booking.getNumPeople());
            data.put("tableName", tableName);

            notification.set("data", data);

            // 🔥 ОТПРАВЛЯЕМ НА КАНАЛ ВЛАДЕЛЬЦА (если нашли)
            if (ownerId != null) {
                String channel = "user_" + ownerId;
                String notificationJson = objectMapper.writeValueAsString(notification);

                // Используем сервис для отправки уведомлений через WebSocket
                int sentCount = webSocketNotificationService.broadcastToChannel(channel, notificationJson);

                log.info("✅ Отправлено WebSocket уведомление о новой брони ID {} на канал владельца {} (user_{}), отправлено: {}",
                        booking.getId(), ownerName, ownerId, sentCount);
            } else {
                log.warn("⚠️ Не найден владелец заведения {} для отправки уведомления", booking.getEstablishmentId());
            }

        } catch (Exception e) {
            log.error("❌ Ошибка отправки WebSocket уведомления: {}", e.getMessage(), e);
        }
    }

    public List<TableEntity> getAvailableTables(Long establishmentId, LocalDateTime requestedTime) {
        LocalDateTime checkEndTime = requestedTime.plusHours(2);

        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(
                establishmentId, requestedTime, checkEndTime
        );

        return tableRepository.findByEstablishmentId(establishmentId).stream()
                .filter(t -> !reservedTableIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public List<BookingDisplayDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Бронирование не найдено"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронирование уже отменено");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private BookingDisplayDto toDisplayDto(BookingEntity b) {
        TableEntity table = tableRepository.findById(b.getTableId()).orElse(null);
        EstablishmentEntity est = establishmentRepository.findById(b.getEstablishmentId()).orElse(null);

        long duration = java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();

        return BookingDisplayDto.builder()
                .id(b.getId())
                .establishmentName(est != null ? est.getName() : "Неизвестно")
                .establishmentAddress(est != null ? est.getAddress() : "")
                .establishmentLatitude(est != null ? est.getLatitude() : 0.0)
                .establishmentLongitude(est != null ? est.getLongitude() : 0.0)
                .tableName(table != null ? table.getName() : "Неизвестно")
                .tableMaxCapacity(table != null ? table.getMaxCapacity() : 0)
                .startTime(b.getStartTime())
                .durationMinutes(duration)
                .status(b.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<OwnerBookingDisplayDto> getPendingBookingsForOwner(Long ownerId) {
        List<Long> establishmentIds = establishmentRepository.findIdsByCreatedUserId(ownerId);

        return bookingRepository.findByEstablishmentIdInAndStatus(establishmentIds, BookingStatus.PENDING)
                .stream()
                .map(this::toOwnerDisplayDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, String statusStr, Long ownerId) {
        BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронь не найдена"));

        EstablishmentEntity est = establishmentRepository.findById(booking.getEstablishmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заведение не найдено"));

        if (!est.getCreatedUserId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Не вы владелец");
        }

        if (status != BookingStatus.CONFIRMED && status != BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Только CONFIRMED или REJECTED");
        }

        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    private OwnerBookingDisplayDto toOwnerDisplayDto(BookingEntity b) {
        EstablishmentEntity est = establishmentRepository.findById(b.getEstablishmentId()).orElse(null);
        TableEntity table = tableRepository.findById(b.getTableId()).orElse(null);
        UserEntity user = userRepository.findById(b.getUserId()).orElse(null);

        return OwnerBookingDisplayDto.builder()
                .id(b.getId())
                .establishmentId(b.getEstablishmentId())
                .establishmentName(est != null ? est.getName() : "—")
                .userName(user != null ? user.getName() : "Гость")
                .guestPhone(b.getGuestPhone())
                .tableName(table != null ? table.getName() : "—")
                .numberOfGuests(b.getNumPeople())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .status(b.getStatus().name())
                .build();
    }
}
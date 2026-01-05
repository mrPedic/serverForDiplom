package com.example.com.venom.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.com.venom.dto.booking.BookingDisplayDto;
import com.example.com.venom.dto.booking.OwnerBookingDisplayDto;
import com.example.com.venom.service.SubscriptionServiceInterface;
import com.example.com.venom.service.WebSocketNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.com.venom.dto.booking.BookingCreationDto;
import com.example.com.venom.entity.BookingEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;
    private final SubscriptionServiceInterface subscriptionService;
    private final WebSocketNotificationService webSocketNotificationServiceKt;

    @PostMapping
    public ResponseEntity<BookingEntity> createBooking(@RequestBody BookingCreationDto dto) {
        log.info("--- [POST /bookings] Attempting to create booking for establishmentId: {} at time: {}",
                dto.getEstablishmentId(), dto.getStartTime());

        BookingEntity createdBooking = bookingService.createBooking(dto);

        log.info("--- [POST /bookings] Successfully created booking ID: {}", createdBooking.getId());

        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @GetMapping("/{establishmentId}/available")
    public ResponseEntity<List<TableEntity>> getAvailableTables(
            @PathVariable Long establishmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        log.info("--- [GET /establishments/{}/available] Checking availability for time: {}",
                establishmentId, dateTime);

        List<TableEntity> availableTables = bookingService.getAvailableTables(establishmentId, dateTime);

        log.info("--- [GET /establishments/{}/available] Found {} available tables.",
                establishmentId, availableTables.size());

        return ResponseEntity.ok(availableTables);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDisplayDto>> getUserBookings(
            @PathVariable Long userId
    ) {
        log.info("--- [GET /bookings/user/{}] Fetching bookings for user.", userId);

        List<BookingDisplayDto> userBookings = bookingService.getUserBookings(userId);

        log.info("--- [GET /bookings/user/{}] Found {} bookings.", userId, userBookings.size());

        return ResponseEntity.ok(userBookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        log.info("--- [DELETE /bookings/{}] Attempting to cancel booking.", bookingId);

        bookingService.cancelBooking(bookingId);

        log.info("--- [DELETE /bookings/{}] Successfully cancelled booking.", bookingId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam("status") String status,
            @RequestParam("ownerId") Long ownerId) {

        log.info("Изменение статуса брони {} на {} от владельца {}", bookingId, status, ownerId);
        bookingService.updateBookingStatus(bookingId, status, ownerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owner/{ownerId}/pending")
    public ResponseEntity<List<OwnerBookingDisplayDto>> getPendingBookingsForOwner(
            @PathVariable Long ownerId) {

        log.info("Запрос pending-броней для владельца {}", ownerId);
        List<OwnerBookingDisplayDto> bookings = bookingService.getPendingBookingsForOwner(ownerId);
        return ResponseEntity.ok(bookings);
    }

    // 🔥 ТЕСТОВЫЙ ENDPOINT ДЛЯ ОТПРАВКИ УВЕДОМЛЕНИЙ
    @PostMapping("/test-notification/{establishmentId}")
    public ResponseEntity<Map<String, Object>> testBookingNotification(
            @PathVariable Long establishmentId,
            @RequestBody Map<String, Object> testData) {

        log.info("Тестовая отправка уведомления для заведения {}", establishmentId);

        try {
            // Формируем JSON уведомления с помощью Jackson ObjectMapper
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("type", "NEW_BOOKING");

            ObjectNode data = objectMapper.createObjectNode();

            Object bookingIdObj = testData.get("bookingId");
            if (bookingIdObj instanceof Number) {
                data.put("bookingId", ((Number) bookingIdObj).longValue());
            } else if (bookingIdObj != null) {
                data.put("bookingId", Long.parseLong(bookingIdObj.toString()));
            } else {
                data.put("bookingId", 9999L);
            }

            data.put("establishmentId", establishmentId);

            Object establishmentNameObj = testData.get("establishmentName");
            data.put("establishmentName", establishmentNameObj != null ?
                    establishmentNameObj.toString() : "Тестовое заведение");

            Object userNameObj = testData.get("userName");
            data.put("userName", userNameObj != null ?
                    userNameObj.toString() : "Тестовый пользователь");

            Object userPhoneObj = testData.get("userPhone");
            data.put("userPhone", userPhoneObj != null ?
                    userPhoneObj.toString() : "+79991112233");

            Object startTimeObj = testData.get("startTime");
            data.put("startTime", startTimeObj != null ?
                    startTimeObj.toString() : LocalDateTime.now().toString());

            Object numPeopleObj = testData.get("numPeople");
            if (numPeopleObj instanceof Number) {
                data.put("numPeople", ((Number) numPeopleObj).intValue());
            } else if (numPeopleObj != null) {
                data.put("numPeople", Integer.parseInt(numPeopleObj.toString()));
            } else {
                data.put("numPeople", 2);
            }

            Object tableNameObj = testData.get("tableName");
            data.put("tableName", tableNameObj != null ?
                    tableNameObj.toString() : "Стол №1");

            notification.set("data", data);

            // Отправляем через notificationHandler
            String channel = "establishment_" + establishmentId;
            String notificationJson = objectMapper.writeValueAsString(notification);

            // Используем сервис для отправки
            int sentCount = webSocketNotificationServiceKt.broadcastToChannel(channel, notificationJson);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Тестовое уведомление отправлено",
                    "channel", channel,
                    "sentTo", sentCount,
                    "notification", notificationJson
            ));

        } catch (Exception e) {
            log.error("Ошибка тестовой отправки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
// BookingController.java — исправляем путь в @GetMapping для available (была опечатка с /establishments), и вызываем новый метод
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

    // 🔥 ИСПРАВЛЕНО: Путь изменен на /bookings/{establishmentId}/available (убрана опечатка с /establishments)
    @GetMapping("/{establishmentId}/available")
    public ResponseEntity<List<TableEntity>> getAvailableTables(
            @PathVariable Long establishmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        log.info("--- [GET /bookings/{}/available] Checking availability for time: {}",
                establishmentId, dateTime);

        List<TableEntity> availableTables = bookingService.getAvailableTables(establishmentId, dateTime);

        log.info("--- [GET /bookings/{}/available] Found {} available tables",
                establishmentId, availableTables.size());

        return ResponseEntity.ok(availableTables);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDisplayDto>> getUserBookings(@PathVariable Long userId) {
        log.info("--- [GET /bookings/user/{}] Fetching bookings for user", userId);

        List<BookingDisplayDto> bookings = bookingService.getUserBookings(userId);

        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId, @RequestParam Long userId) {
        log.info("--- [DELETE /bookings/{}] Cancelling booking for userId: {}", bookingId, userId);

        bookingService.cancelBooking(bookingId, userId);

        log.info("--- [DELETE /bookings/{}] Successfully cancelled", bookingId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/{ownerId}/pending")
    public ResponseEntity<List<OwnerBookingDisplayDto>> getPendingBookingsForOwner(@PathVariable Long ownerId) {
        log.info("--- [GET /bookings/owner/{}/pending] Fetching pending bookings", ownerId);

        List<OwnerBookingDisplayDto> bookings = bookingService.getPendingBookingsForOwner(ownerId);

        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam String status,
            @RequestParam Long ownerId
    ) {
        log.info("--- [PUT /bookings/{}/status] Updating to {} by owner {}", bookingId, status, ownerId);

        BookingEntity booking = bookingService.updateBookingStatus(bookingId, status, ownerId);
        bookingService.notifyUserAboutStatusChange(booking, status);

        return ResponseEntity.ok().build();
    }

    // 🔥 НОВЫЙ ЭНДПОИНТ: Для approved броней (CONFIRMED)
    @GetMapping("/owner/{ownerId}/approved")
    public ResponseEntity<List<OwnerBookingDisplayDto>> getApprovedBookingsForOwner(
            @PathVariable Long ownerId,
            @RequestParam(required = false) Long establishmentId
    ) {
        log.info("--- [GET /bookings/owner/{}/approved] Fetching approved bookings, establishmentId: {}", ownerId, establishmentId);

        List<OwnerBookingDisplayDto> bookings = bookingService.getApprovedBookingsForOwner(ownerId, establishmentId);

        return ResponseEntity.ok(bookings);
    }

    // Тестовый эндпоинт для уведомлений
    @PostMapping("/test/notification/{establishmentId}")
    public ResponseEntity<Map<String, Object>> sendTestNotification(
            @PathVariable Long establishmentId,
            @RequestBody(required = false) Map<String, Object> testData
    ) {
        try {
            if (testData == null) {
                testData = Map.of();
            }

            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("id", "test_" + System.currentTimeMillis());
            notification.put("type", "new_booking");
            notification.put("title", "Тестовое уведомление");
            notification.put("message", "Поступила новая бронь для вашего заведения");

            ObjectNode data = objectMapper.createObjectNode();

            Object bookingIdObj = testData.get("bookingId");
            data.put("bookingId", bookingIdObj != null ?
                    ((Number) bookingIdObj).longValue() : 9999L);

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
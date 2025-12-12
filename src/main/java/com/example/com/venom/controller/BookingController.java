package com.example.com.venom.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.com.venom.dto.booking.BookingDisplayDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.booking.BookingCreationDto;
import com.example.com.venom.entity.BookingEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.service.BookingService; 

import lombok.RequiredArgsConstructor;

/**
 * Контроллер для всех операций, связанных с бронированием.
 * Базовый путь: /bookings
 */
@RestController
@RequestMapping("/bookings") 
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    // Внедрение сервиса с бизнес-логикой
    private final BookingService bookingService; 

    /**
     * Эндпоинт для создания нового бронирования.
     * Путь: POST /bookings
     * * @param dto Данные для создания брони.
     * @return Созданная сущность бронирования (с присвоенным ID).
     */
    @PostMapping
    public ResponseEntity<BookingEntity> createBooking(@RequestBody BookingCreationDto dto) {
        log.info("--- [POST /bookings] Attempting to create booking for establishmentId: {} at time: {}",
                 dto.getEstablishmentId(), dto.getStartTime());
        
        // Вся логика проверки (доступность, вместимость) должна быть в BookingService
        BookingEntity createdBooking = bookingService.createBooking(dto);
        
        log.info("--- [POST /bookings] Successfully created booking ID: {}", createdBooking.getId());
        
        // Возвращаем статус 201 Created
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED); 
    }
    
    // -------------------------------------------------------------------
    // 2. ПОЛУЧЕНИЕ ДОСТУПНЫХ СТОЛОВ (GET)
    // -------------------------------------------------------------------
    /**
     * Эндпоинт для получения списка доступных столиков в заведении на указанное время.
     * Путь: GET /bookings/{establishmentId}/available
     * * @param establishmentId ID заведения.
     * @param dateTime Время, на которое ищем доступные столы.
     * @return Список доступных TableEntity.
     */
    @GetMapping("/{establishmentId}/available") // Итоговый путь: /bookings/{establishmentId}/available
    public ResponseEntity<List<TableEntity>> getAvailableTables(
        @PathVariable Long establishmentId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime
    ) {
        log.info("--- [GET /establishments/{}/available] Checking availability for time: {}", 
                 establishmentId, dateTime);

        // Логика проверки доступности в BookingService
        List<TableEntity> availableTables = bookingService.getAvailableTables(establishmentId, dateTime);

        log.info("--- [GET /establishments/{}/available] Found {} available tables.", 
                 establishmentId, availableTables.size());

        return ResponseEntity.ok(availableTables);
    }

    /**
     * Эндпоинт для получения всех бронирований конкретного пользователя.
     * Путь: GET /bookings/user/{userId}
     * @param userId ID пользователя, чьи бронирования нужно получить.
     * @return Список бронирований пользователя.
     */
    @GetMapping("/user/{userId}") // Итоговый путь: /bookings/user/{userId}
    public ResponseEntity<List<BookingDisplayDto>> getUserBookings(
        @PathVariable Long userId
    ) {
        log.info("--- [GET /bookings/user/{}] Fetching bookings for user.", userId);
        
        // *ВНИМАНИЕ*: Предполагается, что у вас есть метод в BookingService,
        // который возвращает список DTO для отображения.
        List<BookingDisplayDto> userBookings = bookingService.getUserBookings(userId);
        
        log.info("--- [GET /bookings/user/{}] Found {} bookings.", userId, userBookings.size());
        
        return ResponseEntity.ok(userBookings);
    }
    /**
     * Эндпоинт для отмены (удаления) существующего бронирования.
     * Путь: DELETE /bookings/{bookingId}
     * @param bookingId ID бронирования, которое нужно отменить.
     */
    @DeleteMapping("/{bookingId}") // Итоговый путь: /bookings/{bookingId}
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        log.info("--- [DELETE /bookings/{}] Attempting to cancel booking.", bookingId);

        // Вся логика удаления и проверки (например, можно ли отменить)
        // должна быть реализована в BookingService.
        bookingService.cancelBooking(bookingId); 

        log.info("--- [DELETE /bookings/{}] Successfully cancelled booking.", bookingId);

        // Возвращаем статус 204 No Content, который Retrofit ожидает для Response<Unit>
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
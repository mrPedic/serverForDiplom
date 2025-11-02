package com.example.com.venom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.com.venom.StatusForBooking;
import com.example.com.venom.dto.BookingCreationDto;
import com.example.com.venom.entity.BookingEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.repository.BookingRepository;
import com.example.com.venom.repository.TableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository; // Для проверки существования столика

    // ⭐ КОНФИГУРАЦИЯ: Стандартная продолжительность бронирования (например, 1.5 часа)
    private static final long DEFAULT_BOOKING_DURATION_MINUTES = 90; 

    /**
     * Создает новое бронирование.
     */
    public BookingEntity createBooking(BookingCreationDto dto) {
        
        // 1. Проверка существования столика
        Optional<TableEntity> tableOpt = tableRepository.findById(dto.getTableId());
        if (tableOpt.isEmpty() || !tableOpt.get().getEstablishmentId().equals(dto.getEstablishmentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Столик не существует или не принадлежит этому заведению.");
        }
        
        // 2. Расчет времени окончания бронирования
        LocalDateTime endTime = dto.getStartTime().plusMinutes(DEFAULT_BOOKING_DURATION_MINUTES);
        
        // 3. Проверка доступности столика (пересечение)
        if (isTableReserved(dto.getTableId(), dto.getStartTime(), endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выбранный столик уже забронирован на это время.");
        }

        // 4. Маппинг и сохранение
        BookingEntity newBooking = new BookingEntity();
        newBooking.setUserId(dto.getUserId());
        newBooking.setEstablishmentId(dto.getEstablishmentId());
        newBooking.setTableId(dto.getTableId());
        newBooking.setStartTime(dto.getStartTime());
        newBooking.setEndTime(endTime);
        newBooking.setNumPeople(dto.getNumPeople());
        newBooking.setNotes(dto.getNotes());
        // По умолчанию устанавливаем статус PENDING
        newBooking.setStatus(StatusForBooking.PENDING); 

        return bookingRepository.save(newBooking);
    }

    /**
     * Вспомогательный метод для проверки занятости столика.
     */
    private boolean isTableReserved(Long tableId, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        
        // Мы ищем брони, которые пересекаются с запрошенным интервалом:
        // [requestedStartTime, requestedEndTime]
        
        // Занятые столики - это те, у которых:
        // (startTime < requestedEndTime) И (endTime > requestedStartTime)
        
        List<BookingEntity> conflictingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getTableId().equals(tableId))
                .filter(b -> b.getStatus() == StatusForBooking.CONFIRMED || b.getStatus() == StatusForBooking.PENDING)
                .filter(b -> b.getStartTime().isBefore(requestedEndTime) && b.getEndTime().isAfter(requestedStartTime))
                .collect(Collectors.toList());
                
        return !conflictingBookings.isEmpty();
        
        // ⭐ Примечание: В реальном приложении можно использовать более оптимизированный
        // JPA-запрос вместо фильтрации findAll().
    }
    
    /**
     * Поиск доступных столов - необходимо для UI.
     * @param establishmentId ID заведения.
     * @param requestedTime Время, на которое ищем бронь.
     */
    public List<TableEntity> getAvailableTables(Long establishmentId, LocalDateTime requestedTime) {
        
        // Расчет времени окончания брони для проверки конфликтов
        LocalDateTime checkEndTime = requestedTime.plusMinutes(DEFAULT_BOOKING_DURATION_MINUTES);

        // Получаем ID зарезервированных столиков с помощью оптимизированного запроса
        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(
            establishmentId,
            requestedTime,
            checkEndTime,
            StatusForBooking.CONFIRMED,
            StatusForBooking.PENDING
        );
        
        // Получаем все столики и фильтруем те, которые зарезервированы
        List<TableEntity> allTables = tableRepository.findByEstablishmentId(establishmentId);
        
        return allTables.stream()
            .filter(table -> !reservedTableIds.contains(table.getId()))
            .collect(Collectors.toList());
    }

}   
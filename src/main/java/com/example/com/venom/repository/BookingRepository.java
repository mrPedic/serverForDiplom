package com.example.com.venom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.com.venom.StatusForBooking;
import com.example.com.venom.entity.BookingEntity;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    /**
     * Находит ID всех столиков, которые заняты (имеют статус CONFIRMED или PENDING) 
     * в заданном временном интервале.
     * * Мы ищем брони, интервал которых (startTime - endTime) пересекается с:
     * [requestedTime, requestedTime + standardDuration]
     * * @param establishmentId ID заведения.
     * @param requestedTime Время, на которое клиент хочет забронировать.
     * @param checkEndTime Время окончания бронирования для проверки конфликтов.
     * @param confirmedStatus Статус "CONFIRMED".
     * @param pendingStatus Статус "PENDING".
     * @return Список ID забронированных столиков.
     */
    @Query("SELECT b.tableId FROM BookingEntity b WHERE " +
           "b.establishmentId = :establishmentId AND " +
           "(b.status = :confirmedStatus OR b.status = :pendingStatus) AND " +
           "b.startTime < :checkEndTime AND b.endTime > :requestedTime")
    List<Long> findReservedTableIds(
        @Param("establishmentId") Long establishmentId,
        @Param("requestedTime") LocalDateTime requestedTime,
        @Param("checkEndTime") LocalDateTime checkEndTime,
        @Param("confirmedStatus") StatusForBooking confirmedStatus,
        @Param("pendingStatus") StatusForBooking pendingStatus
    );
    
    // Метод для поиска всех бронирований заведения (опционально)
    List<BookingEntity> findByEstablishmentId(Long establishmentId);
    
    // Метод для поиска бронирований пользователя (опционально)
    List<BookingEntity> findByUserId(Long userId);
}
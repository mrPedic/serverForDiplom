package com.example.com.venom.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.com.venom.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.BookingEntity;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    /**
     * Находит ID столиков, которые заняты (PENDING или CONFIRMED) в заданном диапазоне времени
     */
    @Query("SELECT b.tableId FROM BookingEntity b " +
            "WHERE b.establishmentId = :establishmentId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.startTime < :checkEndTime " +
            "AND b.endTime > :requestedTime")
    List<Long> findReservedTableIds(
            @Param("establishmentId") Long establishmentId,
            @Param("requestedTime") LocalDateTime requestedTime,
            @Param("checkEndTime") LocalDateTime checkEndTime
    );

    List<BookingEntity> findByEstablishmentId(Long establishmentId);

    List<BookingEntity> findByUserId(Long userId);

    @Query("SELECT b FROM BookingEntity b WHERE b.establishmentId IN :estIds AND b.status = :status")
    List<BookingEntity> findByEstablishmentIdInAndStatus(
            @Param("estIds") List<Long> establishmentIds,
            @Param("status") BookingStatus status
    );

    int countByEstablishmentIdAndStatus(Long establishmentId, BookingStatus status);
}
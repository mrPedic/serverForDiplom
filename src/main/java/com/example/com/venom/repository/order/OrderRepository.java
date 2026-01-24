package com.example.com.venom.repository.order;

import com.example.com.venom.entity.OrderEntity;
import com.example.com.venom.enums.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OrderEntity> findByEstablishmentIdOrderByCreatedAtDesc(Long establishmentId);

    List<OrderEntity> findByUserIdAndStatusInOrderByCreatedAtDesc(
            Long userId,
            List<OrderStatus> statuses
    );

    List<OrderEntity> findByEstablishmentIdAndStatusOrderByCreatedAtDesc(
            Long establishmentId,
            OrderStatus status
    );

    @Query("SELECT o FROM OrderEntity o WHERE o.establishmentId = :establishmentId " +
            "AND o.deliveryTime BETWEEN :startDate AND :endDate " +
            "ORDER BY o.deliveryTime ASC")
    List<OrderEntity> findOrdersByEstablishmentAndDateRange(
            @Param("establishmentId") Long establishmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.establishmentId = :establishmentId " +
            "AND o.deliveryTime BETWEEN :startDate AND :endDate")
    long countOrdersInTimeSlot(
            @Param("establishmentId") Long establishmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    int countByEstablishmentIdAndStatus(Long establishmentId, OrderStatus status);
}
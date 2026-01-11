package com.example.com.venom.repository.order;

import com.example.com.venom.entity.OrderNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderNotificationRepository extends JpaRepository<OrderNotificationEntity, Long> {

    List<OrderNotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OrderNotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}

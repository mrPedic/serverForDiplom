package com.example.com.venom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.ReviewEntity;

// ⭐ НОВЫЙ РЕПОЗИТОРИЙ
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
    // Дополнительный метод для получения отзывов по ID заведения (для вкладки "Отзывы")
    List<ReviewEntity> findByEstablishmentId(@Param("establishmentId") Long establishmentId);

    // Дополнительный метод для проверки, оставил ли пользователь отзыв
    boolean existsByEstablishmentIdAndCreatedUserId(Long establishmentId, Long createdUserId);

    @Query(value = 
    // Убедитесь, что путь к DTO правильный:
    "SELECT new com.example.com.venom.dto.RatingStats(AVG(r.rating), COUNT(r.id)) " +
    // Убедитесь, что имя сущности 'ReviewEntity' и поле 'establishmentId' правильны
    "FROM ReviewEntity r WHERE r.establishmentId = :establishmentId")
    com.example.com.venom.dto.RatingStats getAverageRatingAndCountByEstablishmentId(@Param("establishmentId") Long establishmentId);
}
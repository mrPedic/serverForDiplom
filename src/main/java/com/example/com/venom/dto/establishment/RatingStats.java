package com.example.com.venom.dto.establishment;

public class RatingStats {
    
    private final Double averageRating;
    private final Long reviewCount;

    /**
     * Конструктор для JPQL SELECT NEW. 
     * Имена и типы аргументов должны строго соответствовать SELECT.
     */
    public RatingStats(Double averageRating, Long reviewCount) {
        // Устанавливаем 0.0 и 0L в случае, если агрегатные функции вернули NULL (например, если отзывов 0)
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0L;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }
}
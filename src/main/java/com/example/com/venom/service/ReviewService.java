package com.example.com.venom.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.com.venom.dto.establishment.RatingStats;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.ReviewEntity;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EstablishmentRepository establishmentRepository;

    /**
     * Сохраняет новый отзыв и обновляет средний рейтинг в сущности заведения.
     * Эту логику мы перенесли из контроллера.
     * * @param review Сущность отзыва.
     * @param establishment Сущность заведения, к которому относится отзыв.
     * @return Сохраненный ReviewEntity.
     */
    @Transactional // Гарантирует атомарность сохранения отзыва и обновления рейтинга
    public ReviewEntity saveReviewAndUpdateEstablishmentRating(ReviewEntity review, EstablishmentEntity establishment) {
        // 1. Сохранение отзыва
        ReviewEntity savedReview = reviewRepository.save(review);
        
        // 2. Пересчет рейтинга
        log.info("Запрос статистики рейтинга для заведения ID: {}", establishment.getId());
        RatingStats stats = reviewRepository.getAverageRatingAndCountByEstablishmentId(establishment.getId());
        
        // 3. Обновление заведения
        establishment.setRating(stats.getAverageRating());
        
        // Сохраняем обновленное заведение (поле rating)
        EstablishmentEntity updatedEstablishment = establishmentRepository.save(establishment);
        log.info("Заведение ID {} обновлено. Новый рейтинг: {}", updatedEstablishment.getId(), updatedEstablishment.getRating());
        
        return savedReview;
    }

    /**
     * Удаляет отзыв по ID и пересчитывает средний рейтинг в сущности заведения.
     * * @param reviewId ID отзыва для удаления.
     * @throws IllegalArgumentException если отзыв не найден.
     */
    @Transactional // Гарантирует атомарность удаления отзыва и обновления рейтинга
    public void deleteReviewAndUpdateEstablishmentRating(Long reviewId) {
        log.info("Поиск отзыва ID {} для удаления.", reviewId);
        
        // 1. Находим отзыв, чтобы получить ID заведения
        ReviewEntity reviewToDelete = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв с ID " + reviewId + " не найден."));

        Long establishmentId = reviewToDelete.getEstablishmentId();

        // 2. Удаление отзыва
        reviewRepository.deleteById(reviewId);
        log.info("Отзыв ID {} успешно удален.", reviewId);
        
        // 3. Поиск и обновление заведения
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElse(null); // Если заведение удалено, просто продолжаем.

        if (establishment != null) {
            // 4. Пересчет рейтинга
            log.info("Запрос статистики рейтинга после удаления отзыва ID {} для заведения ID {}.", reviewId, establishmentId);
            RatingStats stats = reviewRepository.getAverageRatingAndCountByEstablishmentId(establishmentId);
            
            // 5. Обновление заведения
            establishment.setRating(stats.getAverageRating());
            establishmentRepository.save(establishment);
            log.info("Рейтинг заведения ID {} обновлен. Новый рейтинг: {}", establishmentId, establishment.getRating());
        } else {
            log.warn("Заведение ID {} не найдено. Пропускаем обновление рейтинга.", establishmentId);
        }
    }
}
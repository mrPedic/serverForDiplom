package com.example.com.venom.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping; // ⭐ ДОБАВЛЕН ИМПОРТ
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.ReviewEntity;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.ReviewRepository;
import com.example.com.venom.service.ReviewService; // ⭐ ДОБАВЛЕН ИМПОРТ СЕРВИСА

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ⭐ ИМПОРТ ДЛЯ ЛОГГИРОВАНИЯ

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j // ⭐ АННОТАЦИЯ ДЛЯ ЛОГГИРОВАНИЯ
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final EstablishmentRepository establishmentRepository;
    private final ReviewService reviewService; // ⭐ ВНЕДРЕНИЕ СЕРВИСА ДЛЯ СЛОЖНОЙ ЛОГИКИ

    // ========================== Создание отзыва (POST) ==========================
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody ReviewEntity review) {

        log.info("Начат процесс создания отзыва для заведения ID: {} пользователем ID: {}", 
                 review.getEstablishmentId(), review.getCreatedUserId());

        review.setId(null); 
        review.setDateOfCreation(LocalDateTime.now());

        // 1. Проверка обязательных полей
        if (review.getEstablishmentId() == null || review.getCreatedUserId() == null || 
            review.getRating() == null || review.getReviewText() == null || 
            review.getRating() < 1.0f || review.getRating() > 5.0f) {
            
            log.warn("Валидация полей не пройдена. Полученные данные: EstablishmentId={}, UserId={}, Rating={}",
                     review.getEstablishmentId(), review.getCreatedUserId(), review.getRating());
            return ResponseEntity.badRequest().body("Необходимо указать ID заведения, ID пользователя, текст отзыва и оценку (от 1.0 до 5.0).");
        }
        
        // 2. Проверка, является ли пользователь владельцем заведения И существует ли заведение
        Optional<EstablishmentEntity> establishmentOpt = establishmentRepository.findById(review.getEstablishmentId());
        if (establishmentOpt.isEmpty()) {
            log.warn("Заведение с ID {} не найдено.", review.getEstablishmentId());
            return ResponseEntity.badRequest().body("Заведение не найдено.");
        }
        
        EstablishmentEntity establishment = establishmentOpt.get(); // Получаем заведение
        log.info("Заведение найдено. Владелец ID: {}", establishment.getCreatedUserId());

        if (establishment.getCreatedUserId().equals(review.getCreatedUserId())) {
            log.warn("Пользователь {} пытается оставить отзыв на свое заведение.", review.getCreatedUserId());
            return ResponseEntity.badRequest().body("Вы не можете оставить отзыв на свое заведение.");
        }

        // 3. Проверка, оставлял ли пользователь уже отзыв
        // if (reviewRepository.existsByEstablishmentIdAndCreatedUserId(review.getEstablishmentId(), review.getCreatedUserId())) {
        //     log.warn("Пользователь {} уже оставлял отзыв на заведение {}.", review.getCreatedUserId(), review.getEstablishmentId());
        //     return ResponseEntity.badRequest().body("Вы уже оставляли отзыв на это заведение.");
        // }

        // 4. Сохранение отзыва и обновление рейтинга
        try {
            log.info("Попытка сохранения отзыва и обновления рейтинга...");
            ReviewEntity savedReview = reviewService.saveReviewAndUpdateEstablishmentRating(review, establishment);
            
            log.info("Отзыв успешно сохранен. ID нового отзыва: {}", savedReview.getId());
            // Готово!
            return ResponseEntity.ok(savedReview); // Возвращаем сохраненный объект
            
        } catch (Exception e) {
            // ⭐ ЭТОТ БЛОК КРИТИЧЕСКИ ВАЖЕН: он поймает ошибки INSERT/FLUSH/JPQL-запроса
            log.error("КРИТИЧЕСКАЯ ОШИБКА при сохранении отзыва или обновлении рейтинга:", e);
            // Возвращаем 500 Internal Server Error, т.к. это ошибка сервера, а не Bad Request клиента.
            return ResponseEntity.internalServerError().body("Произошла внутренняя ошибка сервера при сохранении данных.");
        }
    }
    
    // ========================== Получение отзывов по ID заведения (GET) ==========================
    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<ReviewEntity>> getReviewsByEstablishmentId(@PathVariable Long establishmentId) {
        log.info("--- [GET /reviews/establishment/{}] Request received for reviews.", establishmentId);
        List<ReviewEntity> reviews = reviewRepository.findByEstablishmentId(establishmentId);
        log.info("--- [GET /reviews/establishment/{}] Found {} reviews.", establishmentId, reviews.size());
        return ResponseEntity.ok(reviews);
    }
    
    // ========================== Удаление отзыва по ID (DELETE) ==========================
    /**
     * Эндпойнт для удаления отзыва по его ID.
     * После удаления отзыва, рейтинг заведения должен быть пересчитан.
     * Соответствует DELETE /reviews/{id} в ApiService.
     * @param id ID отзыва для удаления.
     * @return 204 No Content в случае успеха или 404/500 в случае ошибки.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("--- [DELETE /reviews/{}] Request received for deletion.", id);

        if (id == null || id <= 0) {
            log.warn("--- [DELETE /reviews/{}] Invalid review ID provided.", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            // ⭐ Делегируем логику удаления и пересчета рейтинга сервису
            reviewService.deleteReviewAndUpdateEstablishmentRating(id);
            log.info("--- [DELETE /reviews/{}] Review successfully deleted and establishment rating updated.", id);
            return ResponseEntity.noContent().build(); // 204 No Content - стандартный ответ для успешного удаления
        } catch (IllegalArgumentException e) {
            // Это может быть выброшено, если отзыв не найден
            log.warn("--- [DELETE /reviews/{}] Review not found: {}", id, e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception e) {
            log.error("--- [DELETE /reviews/{}] Error deleting review: {}", id, e.getMessage(), e);
            // Возвращаем 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
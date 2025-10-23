package com.example.com.venom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // *id отзыва

    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId; // *id заведения

    @Column(name = "created_user_id", nullable = false)
    private Long createdUserId; // *id пользователя

    @Column(nullable = false)
    private Float rating; // *оценка (1.0 - 5.0)

    @Column(name = "review_text", columnDefinition = "TEXT", nullable = false)
    private String reviewText; // *Сам текст отзыва

    // Фото в виде строки Base64. ColumnDefinition = "TEXT" для больших строк.
    @Column(name = "photo_base64", columnDefinition = "TEXT")
    private String photoBase64; 

    @Column(name = "date_of_creation", nullable = false)
    private LocalDateTime dateOfCreation = LocalDateTime.now(); // Дата создания заведения (лучше использовать LocalDateTime)

    // --- Конструкторы, Геттеры и Сеттеры ---

    public ReviewEntity() {}

    // Конструктор для создания нового отзыва
    public ReviewEntity(Long establishmentId, Long createdUserId, Float rating, String reviewText, String photoBase64) {
        this.establishmentId = establishmentId;
        this.createdUserId = createdUserId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.photoBase64 = photoBase64;
    }

    // Полный конструктор для обновления или получения
    public ReviewEntity(Long id, Long establishmentId, Long createdUserId, Float rating, String reviewText, String photoBase64, LocalDateTime dateOfCreation) {
        this.id = id;
        this.establishmentId = establishmentId;
        this.createdUserId = createdUserId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.photoBase64 = photoBase64;
        this.dateOfCreation = dateOfCreation;
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEstablishmentId() { return establishmentId; }
    public void setEstablishmentId(Long establishmentId) { this.establishmentId = establishmentId; }
    public Long getCreatedUserId() { return createdUserId; }
    public void setCreatedUserId(Long createdUserId) { this.createdUserId = createdUserId; }
    public Float getRating() { return rating; }
    public void setRating(Float rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    public LocalDateTime getDateOfCreation() { return dateOfCreation; }
    public void setDateOfCreation(LocalDateTime dateOfCreation) { this.dateOfCreation = dateOfCreation; }
}
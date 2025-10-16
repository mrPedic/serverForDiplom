package com.example.com.venom.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column; // Оставляем импорт, так как он может использоваться в других методах
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "establishments")
public class EstablishmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double latitude;

    private Double longitude;

    private String address;

    private String description;

    private Double rating = 0.0; // Инициализируем 0.0 по умолчанию

    private Long idMenu; // ID меню

    @Column(name = "created_user_id")
    private Long createdUserId;

    @Enumerated(EnumType.STRING) // Сохраняем ENUM как строку
    private EstablishmentStatus status; // ⭐ ДОБАВЛЕНО ПОЛЕ СТАТУСА

    @Column(name="date")
    // ⭐ УДАЛЕНИЕ @JsonIgnore: теперь используем DTO для контроля вывода данных,
    // что позволяет нам избежать конфликтов при операциях PUT/GET.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation = LocalDate.now(); // Инициализируем текущей датой

    // ----------------------------------------------------------------------
    // КОНСТРУКТОРЫ
    // ----------------------------------------------------------------------

    // Конструктор по умолчанию (требуется JPA)
    public EstablishmentEntity() {}

    /**
     * Конструктор для создания нового заведения.
     * @param name Название заведения.
     * @param latitude Широта.
     * @param longitude Долгота.
     * @param address Адрес.
     * @param description Описание.
     * @param createdUserId ID пользователя, создавшего заведение.
     */
    public EstablishmentEntity(
            String name,
            Double latitude,
            Double longitude,
            String address,
            String description,
            Long createdUserId) 
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        
        // --- Фиксированные значения для нового заведения ---
        this.rating = 0.0;
        this.dateOfCreation = LocalDate.now();
        this.idMenu = this.id; 
        
        // ⭐ КРИТИЧЕСКИ ВАЖНОЕ ПОЛЕ
        this.createdUserId = createdUserId;
        
        // ⭐ ОЧИЩЕННАЯ ИНИЦИАЛИЗАЦИЯ СТАТУСА
        this.status = EstablishmentStatus.PENDING_APPROVAL; 
    }
    
    // ----------------------------------------------------------------------
    // ГЕТТЕРЫ И СЕТТЕРЫ (ДОПОЛНЕНЫ)
    // ----------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    // ⭐ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ID MENU И CREATED USER ID
    public Long getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(Long idMenu) {
        this.idMenu = idMenu;
    }

    public Long getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(Long createdUserId) {
        this.createdUserId = createdUserId;
    }

    // ⭐ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ СТАТУСА
    public EstablishmentStatus getStatus() {
        return status;
    }

    public void setStatus(EstablishmentStatus status) {
        this.status = status;
    }
}

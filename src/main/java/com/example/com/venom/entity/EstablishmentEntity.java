package com.example.com.venom.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
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

    private Long createdUserId;

    @Enumerated(EnumType.STRING) // Сохраняем ENUM как строку
    private EstablishmentStatus status; // ⭐ ДОБАВЛЕНО ПОЛЕ СТАТУСА

    @Column(name="date")
    @JsonIgnore
    // ⭐ ЯВНОЕ УКАЗАНИЕ ФОРМАТА: 
    // Гарантирует, что дата будет сериализована как строка "yyyy-MM-dd", 
    // обходя возможные проблемы конфигурации на клиенте.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation = LocalDate.now(); // Инициализируем текущей датой

    // ----------------------------------------------------------------------
    // КОНСТРУКТОРЫ
    // ----------------------------------------------------------------------

    // Конструктор по умолчанию (требуется JPA)
    public EstablishmentEntity() {}

    // Конструктор для создания нового заведения (ИСПРАВЛЕН)
    public EstablishmentEntity(
            String name,
            Double latitude,
            Double longitude,
            String address,
            String description,
            Long createdUserId) // ⭐ ДОБАВЛЕН СТАТУС
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        this.rating = 0.0;
        this.dateOfCreation = LocalDate.now();
        // ID меню должен быть установлен после сохранения, или через отдельный сервис, 
        // поэтому не устанавливаем его здесь, как это делалось ошибочно с this.id
        this.idMenu = null; 
        this.createdUserId = createdUserId;
        this.status = status = EstablishmentStatus.PENDING_APPROVAL; // Устанавливаем статус
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

    // ⭐ ДОБАВЛЕНЫ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ID MENU И CREATED USER ID
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

    // ⭐ ДОБАВЛЕНЫ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ СТАТУСА
    public EstablishmentStatus getStatus() {
        return status;
    }

    public void setStatus(EstablishmentStatus status) {
        this.status = status;
    }
}


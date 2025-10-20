package com.example.com.venom.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    private Double rating = 0.0;

    private Long idMenu;

    @Column(name = "created_user_id")
    private Long createdUserId;

    @Enumerated(EnumType.STRING)
    private EstablishmentStatus status;

    // ⭐ НОВОЕ ПОЛЕ ТИПА
    @Enumerated(EnumType.STRING)
    private EstablishmentType type; 

    @Column(name="date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation = LocalDate.now();

    // ----------------------------------------------------------------------
    // КОНСТРУКТОРЫ
    // ----------------------------------------------------------------------

    public EstablishmentEntity() {}

    /**
     * Конструктор для создания нового заведения (ОБНОВЛЕН).
     * @param name Название заведения.
     * @param latitude Широта.
     * @param longitude Долгота.
     * @param address Адрес.
     * @param description Описание.
     * @param createdUserId ID пользователя, создавшего заведение.
     * @param type Тип заведения. // <-- НОВЫЙ ПАРАМЕТР
     */
    public EstablishmentEntity(
            String name,
            Double latitude,
            Double longitude,
            String address,
            String description,
            Long createdUserId,
            EstablishmentType type) // <-- НОВЫЙ ПАРАМЕТР
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;

        // --- Фиксированные значения для нового заведения ---
        this.rating = 0.0;
        this.dateOfCreation = LocalDate.now();
        this.idMenu = null; // Лучше null, чем id, который еще не присвоен!
        
        this.createdUserId = createdUserId;
        this.status = EstablishmentStatus.PENDING_APPROVAL; 
        
        // ⭐ ИНИЦИАЛИЗАЦИЯ НОВОГО ПОЛЯ
        this.type = type; 
    }
    
    // ----------------------------------------------------------------------
    // ГЕТТЕРЫ И СЕТТЕРЫ (ДОПОЛНЕНЫ)
    // ----------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public LocalDate getDateOfCreation() { return dateOfCreation; }
    public void setDateOfCreation(LocalDate dateOfCreation) { this.dateOfCreation = dateOfCreation; }
    public Long getIdMenu() { return idMenu; }
    public void setIdMenu(Long idMenu) { this.idMenu = idMenu; }
    public Long getCreatedUserId() { return createdUserId; }
    public void setCreatedUserId(Long createdUserId) { this.createdUserId = createdUserId; }
    public EstablishmentStatus getStatus() { return status; }
    public void setStatus(EstablishmentStatus status) { this.status = status; }
    
    // ⭐ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ТИПА
    public EstablishmentType getType() { return type; }
    public void setType(EstablishmentType type) { this.type = type; }
}
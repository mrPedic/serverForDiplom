package com.example.com.venom.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @Column(name = "status", nullable = false)
    private EstablishmentStatus status;

    @Enumerated(EnumType.STRING)
    private EstablishmentType type; 

    @Column(name="date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation = LocalDate.now();

    @ElementCollection 
    @CollectionTable(
        name = "establishment_photos", 
        joinColumns = @JoinColumn(name = "establishment_id")
    )
    @Column(name = "base64_photo", columnDefinition = "TEXT")
    private List<String> photoBase64s;


    // ----------------------------------------------------------------------
    // КОНСТРУКТОРЫ
    // ----------------------------------------------------------------------

    public EstablishmentEntity() {}

    /**
     * Конструктор для создания нового заведения (ОБНОВЛЕН).
     */
    public EstablishmentEntity(
            String name,
            Double latitude,
            Double longitude,
            String address,
            String description,
            Long createdUserId,
            EstablishmentType type,
            List<String> photoBase64s) // ⭐ НОВЫЙ ПАРАМЕТР
    {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;

        // --- Фиксированные значения для нового заведения ---
        this.rating = 0.0;
        this.dateOfCreation = LocalDate.now();
        this.idMenu = null; 
        
        this.createdUserId = createdUserId;
        this.status = EstablishmentStatus.PENDING_APPROVAL; 
        
        this.type = type; 
        // ⭐ ИНИЦИАЛИЗАЦИЯ НОВОГО ПОЛЯ (может быть null или пустой строкой)
        this.photoBase64s = photoBase64s; 
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
    public EstablishmentType getType() { return type; }
    public void setType(EstablishmentType type) { this.type = type; }
    public List<String> getPhotoBase64s() { return photoBase64s; }
    public void setPhotoBase64s(List<String> photoBase64s) { this.photoBase64s = photoBase64s; }

}
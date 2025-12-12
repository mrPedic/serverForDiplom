package com.example.com.venom.dto;

import com.example.com.venom.enums.EstablishmentType;


public class EstablishmentFavoriteDto {
    private Long id;
    private String name;
    private String address;
    private Double rating;
    private EstablishmentType type;
    private String photoBase64; 

    // Конструктор
    public EstablishmentFavoriteDto(Long id, String name, String address, Double rating, EstablishmentType type, String photoBase64) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.type = type;
        this.photoBase64 = photoBase64;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public EstablishmentType getType() { return type; }
    public void setType(EstablishmentType type) { this.type = type; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
}
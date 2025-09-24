package com.example.com.venom.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "establishment")
public class EstablishmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("Id")
    private Long Id;

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("Latitude")
    private Double Latitude;

    @JsonProperty("Longtitude")
    private Double Longtitude;

    @JsonProperty("Address")
    private String Address;

    @JsonProperty("Description")
    private String Description;

    @JsonProperty("Rating")
    private Double Rating;

	public EstablishmentEntity() {}

	public EstablishmentEntity(String name, Double latitude, Double longtitude, String address, String description, Double rating) {
		this.Name = name;
		this.Latitude = latitude;
		this.Longtitude = longtitude;
		this.Address = address;
		this.Description = description;
		this.Rating = rating;
	}

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        this.Latitude = latitude;
    }

    public Double getLongtitude() {
        return Longtitude;
    }

    public void setLongtitude(Double longtitude) {
        this.Longtitude = longtitude;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        this.Address = address;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

    public Double getRating() {
        return Rating;
    }

    public void setRating(Double rating) {
        this.Rating = rating;
    }

}
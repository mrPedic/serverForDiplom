package com.example.com.venom.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name="date")
    private LocalDate dateOfCreation = LocalDate.now();

    public UserEntity() {
        this.name = "Default_Name";
        this.role = Role.UnRegistered;
    }
    
    public UserEntity(String name, String login, String password, Role role) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorites", // Имя промежуточной таблицы в БД
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "establishment_id")
    )
    @JsonIgnore // Мы не хотим отдавать список избранного при каждом запросе User, это тяжело
    private Set<EstablishmentEntity> favorites = new HashSet<>();

    // Getters and Setters для favorites
    public Set<EstablishmentEntity> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<EstablishmentEntity> favorites) {
        this.favorites = favorites;
    }

    public Long getId(){ return this.id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public String getPassword() { return password; }
    public LocalDate getDate(){return dateOfCreation;}

    public void setId(Long id){this.id = id;}
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setDateCreation(LocalDate date){this.dateOfCreation = date;}

    public enum Role{
        Registered,
        UnRegistered,
        AdminOfInstitution,
        AdminOfApp
    }
}
package com.example.com.venom.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class AccountEntity {
    
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

    public AccountEntity() {
        this.name = "Default_Name";
        this.role = Role.UnRegistered;
    }
    
    public AccountEntity(String name, String login, String password, Role role) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.role = role;
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
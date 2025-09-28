package com.example.com.venom.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
public class AccountEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("Id")
    @Column(name = "Id")
    private Long id;

    @JsonProperty("Name")
    @Column(name = "Name")
    private String name;

    @JsonProperty("Login")
    @Column(name = "Login")
    private String login;

    @JsonProperty("Password")
    @Column(name = "Password")
    private String password;

    @Enumerated(EnumType.STRING)
    @JsonProperty("Role")
    @Column(name = "Role")
    private Role role;

    @Column(name = "Email", unique = true)
    @JsonProperty("Email")
    private String email;

    public void setId(Long id) { this.id = id; }

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return email; }

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
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public enum Role{
        Registered,
        UnRegistered,
        AdminOfInstitution,
        AdminOfApp
    }

}
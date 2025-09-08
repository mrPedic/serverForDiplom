package com.example.com.venom.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class AccountEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String Name;
    private String Login;
    private String Password;
    private Role Role;

    public AccountEntity() {
        this.Name = "Default_Name";
        this.Role = Role.NotRegistredUser;
    }
    
    public AccountEntity(String Name, String Login, String Password, Role role) {
        this.Name = Name;
        this.Login = Login;
        this.Password = Password;
        this.Role = role;
    }

    public String getLogin() {
        return Login;
    }
    public void setLogin(String login) {
        Login = login;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public String getPassword() {
        return Password;
    }
    public void setPassword(String password) {
        Password = password;
    }
    public Role getRole() {
        return Role;
    }
    public void setRole(Role role) {
        this.Role = role;
    }

    public enum Role {
        NotRegistredUser,
        RegistredUser,
        AdministratorOfInstitution,
        AdministratorOfApp
    }

}
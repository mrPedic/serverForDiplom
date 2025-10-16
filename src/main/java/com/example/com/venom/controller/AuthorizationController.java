package com.example.com.venom.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.entity.AccountEntity.Role;
import com.example.com.venom.repository.AccountRepository;

@RestController
public class AuthorizationController {

    @Autowired
    private AccountRepository accountRepository;

    //==================================    Регистрация    ==================================
    @PostMapping("/auth/register")
    public ResponseEntity<?> Register(@RequestBody Map<String, String> request ){
        String login = request.get("login");
        String name = request.get("name");
        String password = request.get("password");

        Optional<AccountEntity> existing = accountRepository.findByLogin(login);
        if (existing.isPresent()){
            return ResponseEntity.badRequest().body("Пользователь с таким логином уже существует");
            }
            AccountEntity accountEntity = new AccountEntity( name, login, password, Role.Registered);
        AccountEntity savedAccount = accountRepository.save(accountEntity);
        return ResponseEntity.ok(savedAccount.getId());
    }

    //==================================    Авторизация    ==================================
    @PostMapping("/auth/login")
    public ResponseEntity<?> Login(@RequestBody Map<String, String> request ){
        String login = request.get("login");
        String password = request.get("login");

        Optional<AccountEntity> userOptional = accountRepository.findByLoginAndPassword(login,password);
        if(userOptional.isPresent()){
            return ResponseEntity.ok(userOptional.get());
        }
        else{
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        }
    }
}
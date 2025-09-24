package com.example.com.venom.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.repository.AccountRepository;

@RestController
public class AuthorizationController {

    @Autowired
    private AccountRepository accountRepository;

    //==================================    Регистрация    ==================================
    @PostMapping("/auth/register")
    public ResponseEntity<?> Register(@RequestBody AccountEntity accountEntity ){
        Optional<AccountEntity> existing = accountRepository.findByLogin(accountEntity.getLogin());
        if (existing.isPresent()){
            return ResponseEntity.badRequest().body("Пользователь с таким логином уже существует");
            }
        AccountEntity savedAccount = accountRepository.save(accountEntity);
        return ResponseEntity.ok(savedAccount);
    }

    //==================================    Авторизация    ==================================
    @PostMapping("/auth/login")
    public ResponseEntity<?> Login(@RequestBody AccountEntity accountEntity ){

        Optional<AccountEntity> userOptional = accountRepository.findByLoginAndPassword(accountEntity.getLogin(),accountEntity.getPassword());
        if(userOptional.isPresent()){
            return ResponseEntity.ok(userOptional.get());
        }
        else{
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        }
    }
}
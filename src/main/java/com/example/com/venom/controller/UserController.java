package com.example.com.venom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.repository.AccountRepository;

import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

    //==================================    Получение своих данных    ==================================
    @GetMapping("/user/me")
    public ResponseEntity<?> GetMe(@RequestParam("id") Long id){
        Optional<AccountEntity> existing = accountRepository.findById(id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        } else {
            return ResponseEntity.badRequest().body("Пользователя с таким id не существует");
        }
    }

    //==================================    Обновление данных пользователя    ==================================
    @PutMapping("/user/me")
    public ResponseEntity<?> UpdateMe(@RequestBody AccountEntity accountEntity){
        Optional<AccountEntity> existing = accountRepository.findById(accountEntity.getId());
        if (existing.isPresent()) {
            AccountEntity accountToUpdate = existing.get();

            if (accountEntity.getLogin() != null) {
                accountToUpdate.setLogin(accountEntity.getLogin());
            }
            if (accountEntity.getEmail() != null) {
                accountToUpdate.setEmail(accountEntity.getEmail());
            }
            if (accountEntity.getRole() != null) {
                accountToUpdate.setRole(accountEntity.getRole());
            }
            if (accountEntity.getName() != null) {
                accountToUpdate.setName(accountEntity.getName());
            }
            accountRepository.save(accountToUpdate);

            return ResponseEntity.ok().body("Данные успешно обновлены");
        } else {
            return ResponseEntity.badRequest().body("Пользователя с таким id не существует");
        }
    }

    //==================================    Обновление пароля пользователя    ==================================
    @PutMapping("/user/me/password")
    public ResponseEntity<?> UpdateMePassword(@RequestBody AccountEntity accountEntity){
        Optional<AccountEntity> existing = accountRepository.findById(accountEntity.getId());
        if (existing.isPresent()) {
            AccountEntity accountToUpdate = existing.get();

            if(accountEntity.getPassword() != null){
                accountToUpdate.setPassword(accountEntity.getPassword());
            }

            accountRepository.save(accountToUpdate);

            return ResponseEntity.ok().body("Данные успешно обновлены");
        } else {
            return ResponseEntity.badRequest().body("Пользователя с таким id не существует");
        }
    }

    //==================================    Удаление пользователя по id    ==================================
    @DeleteMapping("/user/me")
    public ResponseEntity<?> DeleteById (@RequestParam("id") Long id){
        Optional<AccountEntity> existing = accountRepository.findById(id);
        if(existing.isPresent()){
            accountRepository.delete(existing.get());
            return ResponseEntity.ok().body("Пользователь с таким id был удален");
        }
        else{
            return ResponseEntity.badRequest().body("Не удалось нати пользователя с таким id");
        }
    }
}
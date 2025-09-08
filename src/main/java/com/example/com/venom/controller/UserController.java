package com.example.com.venom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.repository.AccountRepository;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/api/accounts")
    public ResponseEntity<AccountEntity> createAccount(@RequestBody AccountEntity account) {
        AccountEntity savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @GetMapping("/api/accounts")
    public ResponseEntity<List<AccountEntity>> getAllAccounts() {
        List<AccountEntity> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/api/accounts/{id}")
    public ResponseEntity<AccountEntity> getAccount(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(account -> ResponseEntity.ok(account))
                .orElse(ResponseEntity.notFound().build());
    }
}   
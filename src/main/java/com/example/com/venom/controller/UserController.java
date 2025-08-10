package com.example.com.venom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.com.venom.dto.UserRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @PostMapping(value = "/api/users", produces = "application/json")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody UserRequest userRequest) {
        System.out.println("Получено сообщение от клиента: " + userRequest.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Сообщение '" + userRequest.getMessage() + "' успешно получено сервером!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("")
    public String showInfo(){
        return "Hello World";
    }
}


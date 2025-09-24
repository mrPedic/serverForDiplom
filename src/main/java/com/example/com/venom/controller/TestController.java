package com.example.com.venom.controller;

import org.springframework.web.bind.annotation.*;

@RestController 
public class TestController {
    
    @GetMapping("/test/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/")
    public String showInfo(){
        return "Hello World";
    }    
}

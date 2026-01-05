package com.example.com.venom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.boot.context.properties.bind.Bindable.mapOf;

@RestController
public class TestController {

    @GetMapping("/test/ping")
    public String TestPing(){
        return "pong";
    }

    @GetMapping("/")
    public ResponseEntity<String> Default() throws IOException {
        File htmlFile = new File("C:\\Users\\vladv\\сервер\\venom\\src\\main\\resources\\static\\hello.html");
        String content = Files.readString(htmlFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }


    @GetMapping("/test/")
    public String home() {
        return "Venom server is running! " + LocalDateTime.now();
    }
}
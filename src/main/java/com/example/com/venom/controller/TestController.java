package com.example.com.venom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/ping")
    public String TestPing(){
        return "pong";
    }

    @GetMapping("/")
    public ResponseEntity<String> Default() throws IOException {
        File htmlFile = new File("C:\\Users\\vladv\\сервер\\venom\\src\\main\\java\\com\\example\\com\\hello.html");
        String content = Files.readString(htmlFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    
    

    // @GetMapping("/")
    // public ResponseEntity<String> Default() throws IOException {
    //     File htmlFile = new File("C:\\Users\\vladv\\сервер\\venom\\src\\main\\resources\\static\\test_ws.html");
    //     String content = Files.readString(htmlFile.toPath());
    //     return ResponseEntity.ok()
    //             .contentType(MediaType.TEXT_HTML)
    //             .body(content);
    // }
}
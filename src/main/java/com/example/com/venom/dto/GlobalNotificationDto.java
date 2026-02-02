package com.example.com.venom.dto;

import lombok.Data;

@Data
public class GlobalNotificationDto {
    private String title;
    private String message;
    private String target; // Примеры: "all_users", "specific_user:5"
}
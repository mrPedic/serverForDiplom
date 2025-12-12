package com.example.com.venom.service;

// Пример методов, которые должны быть в FavoriteService.java

import java.util.List;

import com.example.com.venom.dto.EstablishmentDisplayDto;


public interface FavoriteService {
    
    // 1. Получение избранного
    List<EstablishmentDisplayDto> findUserFavorites(Long userId);

    // 2. Добавление
    void addFavorite(Long userId, Long establishmentId) throws Exception;

    // 3. Удаление
    void removeFavorite(Long userId, Long establishmentId) throws Exception;
}
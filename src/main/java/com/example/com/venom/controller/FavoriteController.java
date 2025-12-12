// package com.example.com.venom.controller;

// import java.util.List;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.com.venom.dto.EstablishmentDisplayDto; // DTO для отображения избранных
// import com.example.com.venom.dto.EstablishmentFavoriteDto; // DTO для добавления/удаления
// import com.example.com.venom.service.FavoriteService; // Новый сервис для логики избранного

// import lombok.RequiredArgsConstructor;

// /**
//  * REST Controller для управления списком избранных заведений пользователя.
//  * Путь: /favorites
//  */
// @RestController
// @RequestMapping("/favorites") // ⭐ Основной маршрут
// @RequiredArgsConstructor
// public class FavoriteController {

//     private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

//     // Внедряем новый сервис для избранного (предполагаем, что он существует)
//     private final FavoriteService favoriteService; 

//     // ========================== Получение избранных заведений пользователя ==========================
//     // Соответствует: @GET("/favorites/user/{userId}")
//     @GetMapping("/user/{userId}")
//     public ResponseEntity<List<EstablishmentDisplayDto>> getFavoritesByUserId(@PathVariable Long userId) {
//         log.info("--- [GET /favorites/user/{}] Request to get favorites.", userId);
        
//         List<EstablishmentDisplayDto> favorites = favoriteService.findUserFavorites(userId);
        
//         log.info("--- [GET /favorites/user/{}] Found {} favorite establishments.", userId, favorites.size());
//         return ResponseEntity.ok(favorites);
//     }

//     // ========================== Добавление заведения в избранное ==========================
//     // Соответствует: @POST("/favorites")
//     @PostMapping
//     public ResponseEntity<?> addFavorite(@RequestBody EstablishmentFavoriteDto request) {
//         log.info("--- [POST /favorites] Adding Establishment ID {} for User ID {}.", 
//                  request.getEstablishmentId(), request.getUserId());
        
//         try {
//             favoriteService.addFavorite(request.getUserId(), request.getEstablishmentId());
//             return ResponseEntity.ok("Заведение успешно добавлено в избранное.");
//         } catch (Exception e) {
//             log.warn("--- [POST /favorites] Failed to add favorite: {}", e.getMessage());
//             return ResponseEntity.badRequest().body(e.getMessage());
//         }
//     }

//     // ========================== Удаление заведения из избранного ==========================
//     // Соответствует: @DELETE("/favorites")
//     // Используем @RequestBody, так как ваш клиентский код (ApiService.kt) отправляет тело запроса.
//     @DeleteMapping
//     public ResponseEntity<?> removeFavorite(@RequestBody EstablishmentFavoriteDto request) {
//         log.info("--- [DELETE /favorites] Removing Establishment ID {} for User ID {}.", 
//                  request.getEstablishmentId(), request.getUserId());
        
//         try {
//             favoriteService.removeFavorite(request.getUserId(), request.getEstablishmentId());
//             return ResponseEntity.ok("Заведение успешно удалено из избранного.");
//         } catch (Exception e) {
//             log.warn("--- [DELETE /favorites] Failed to remove favorite: {}", e.getMessage());
//             return ResponseEntity.badRequest().body(e.getMessage());
//         }
//     }
// }
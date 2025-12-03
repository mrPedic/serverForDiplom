package com.example.com.venom.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.EstablishmentFavoriteDto;
import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.AccountRepository;
import com.example.com.venom.repository.EstablishmentRepository;

@RestController
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository; 

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

    // 1. Получить список ID избранных заведений (для иконок сердечек)
    @GetMapping("/users/{userId}/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFavoriteIds(@PathVariable Long userId) {
        Optional<AccountEntity> userOpt = accountRepository.findById(userId);
        if (userOpt.isPresent()) {
            Set<EstablishmentEntity> favorites = userOpt.get().getFavorites();
            // Превращаем список объектов в список ID
            List<Long> ids = favorites.stream()
                    .map(EstablishmentEntity::getId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ids);
        }
        return ResponseEntity.badRequest().body("Пользователь не найден");
    }

    // 2. Добавить в избранное
    @PostMapping("/users/{userId}/favorites/{establishmentId}")
    @Transactional 
    public ResponseEntity<?> addFavorite(@PathVariable Long userId, @PathVariable Long establishmentId) {
        Optional<AccountEntity> userOpt = accountRepository.findById(userId);
        Optional<EstablishmentEntity> estOpt = establishmentRepository.findById(establishmentId);

        if (userOpt.isPresent() && estOpt.isPresent()) {
            AccountEntity user = userOpt.get();
            EstablishmentEntity establishment = estOpt.get();

            user.getFavorites().add(establishment); // Добавляем
            accountRepository.save(user); // Сохраняем связь

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Пользователь или заведение не найдены");
    }

    // 3. Удалить из избранного
    @DeleteMapping("/users/{userId}/favorites/{establishmentId}")
    @Transactional 
    public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable Long establishmentId) {
        Optional<AccountEntity> userOpt = accountRepository.findById(userId);
        Optional<EstablishmentEntity> estOpt = establishmentRepository.findById(establishmentId);

        if (userOpt.isPresent() && estOpt.isPresent()) {
            AccountEntity user = userOpt.get();
            EstablishmentEntity establishment = estOpt.get();

            user.getFavorites().remove(establishment); // Удаляем
            accountRepository.save(user); // Сохраняем

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Пользователь или заведение не найдены");
    }

    // 4. Получить ПОЛНЫЙ список DTO для экрана профиля (горизонтальный скролл)
    @GetMapping("/users/{userId}/favorites/list")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFavoriteListDto(@PathVariable Long userId) {
        Optional<AccountEntity> userOpt = accountRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            Set<EstablishmentEntity> favorites = userOpt.get().getFavorites();
            List<EstablishmentFavoriteDto> dtos = new ArrayList<>();

            for (EstablishmentEntity est : favorites) {
                // Берем первое фото из списка, если есть
                String photo = null;
                if (est.getPhotoBase64s() != null && !est.getPhotoBase64s().isEmpty()) {
                    photo = est.getPhotoBase64s().get(0);
                }

                // Маппим Entity в DTO
                dtos.add(new EstablishmentFavoriteDto(
                    est.getId(),
                    est.getName(),
                    est.getAddress(),
                    est.getRating(),
                    est.getType(),
                    photo
                ));
            }
            return ResponseEntity.ok(dtos);
        }
        return ResponseEntity.badRequest().body("Пользователь не найден");
    }
}
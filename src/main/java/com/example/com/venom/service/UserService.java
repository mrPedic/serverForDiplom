package com.example.com.venom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.com.venom.dto.EstablishmentFavoriteDto;
import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.AccountRepository;
import com.example.com.venom.repository.EstablishmentRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor  // Автоматически инжектит репозитории (лучше @Autowired)
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AccountRepository accountRepository;
    private final EstablishmentRepository establishmentRepository;

    // Получить свои данные пользователя
    public Optional<AccountEntity> getUserById(Long id) {
        log.info("--- [SERVICE] getUserById: Received id={}", id);
        Optional<AccountEntity> user = accountRepository.findById(id);
        if (user.isPresent()) {
            log.info("--- [SERVICE] getUserById: Found user with id={}", id);
        } else {
            log.warn("--- [SERVICE] getUserById: User not found with id={}", id);
        }
        return user;
    }

    // Обновить данные пользователя (кроме пароля)
    @Transactional
    public AccountEntity updateUser(AccountEntity updatedUser) {
        log.info("--- [SERVICE] updateUser: Received id={}", updatedUser.getId());
        AccountEntity existing = accountRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователя с таким id не существует"));

        if (updatedUser.getLogin() != null) existing.setLogin(updatedUser.getLogin());
        if (updatedUser.getRole() != null) existing.setRole(updatedUser.getRole());
        if (updatedUser.getName() != null) existing.setName(updatedUser.getName());

        AccountEntity saved = accountRepository.save(existing);
        log.info("--- [SERVICE] updateUser: Updated user with id={}", saved.getId());
        return saved;
    }

    // Обновить пароль пользователя
    @Transactional
    public AccountEntity updateUserPassword(AccountEntity userWithPassword) {
        log.info("--- [SERVICE] updateUserPassword: Received id={}", userWithPassword.getId());
        AccountEntity existing = accountRepository.findById(userWithPassword.getId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователя с таким id не существует"));

        if (userWithPassword.getPassword() != null) {
            existing.setPassword(userWithPassword.getPassword());  // TODO: Хэшируй пароль!
        }

        AccountEntity saved = accountRepository.save(existing);
        log.info("--- [SERVICE] updateUserPassword: Updated password for user id={}", saved.getId());
        return saved;
    }

    // Удалить пользователя по ID
    @Transactional
    public void deleteUserById(Long id) {
        log.info("--- [SERVICE] deleteUserById: Received id={}", id);
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Не удалось найти пользователя с таким id");
        }
        accountRepository.deleteById(id);
        log.info("--- [SERVICE] deleteUserById: Deleted user with id={}", id);
    }

    // 1. Получить список ID избранных заведений
    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(Long userId) {
        log.info("--- [SERVICE] getFavoriteIds: Received userId={}", userId);
        AccountEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Set<EstablishmentEntity> favorites = user.getFavorites();
        List<Long> ids = favorites.stream()
                .map(EstablishmentEntity::getId)
                .collect(Collectors.toList());
        log.info("--- [SERVICE] getFavoriteIds: Found {} favorite IDs for userId={}", ids.size(), userId);
        return ids;
    }

    // 2. Добавить в избранное
    @Transactional
    public void addFavorite(Long userId, Long establishmentId) {
        log.info("--- [SERVICE] addFavorite: Received userId={}, establishmentId={}", userId, establishmentId);
        AccountEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));

        if (user.getFavorites().contains(establishment)) {
            throw new IllegalArgumentException("Заведение уже в избранном");
        }

        user.getFavorites().add(establishment);
        accountRepository.save(user);
        log.info("--- [SERVICE] addFavorite: Added favorite for userId={}, establishmentId={}", userId, establishmentId);
    }

    // 3. Удалить из избранного
    @Transactional
    public void removeFavorite(Long userId, Long establishmentId) {
        log.info("--- [SERVICE] removeFavorite: Received userId={}, establishmentId={}", userId, establishmentId);
        AccountEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));

        if (!user.getFavorites().remove(establishment)) {
            throw new IllegalArgumentException("Заведение не в избранном");
        }

        accountRepository.save(user);
        log.info("--- [SERVICE] removeFavorite: Removed favorite for userId={}, establishmentId={}", userId, establishmentId);
    }

    // 4. Получить полный список DTO для экрана профиля
    @Transactional(readOnly = true)
    public List<EstablishmentFavoriteDto> getFavoriteListDto(Long userId) {
        log.info("--- [SERVICE] getFavoriteListDto: Received userId={}", userId);
        AccountEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Set<EstablishmentEntity> favorites = user.getFavorites();
        List<EstablishmentFavoriteDto> dtos = new ArrayList<>();
        for (EstablishmentEntity est : favorites) {
            String photo = null;
            if (est.getPhotoBase64s() != null && !est.getPhotoBase64s().isEmpty()) {
                photo = est.getPhotoBase64s().get(0);
            }
            dtos.add(new EstablishmentFavoriteDto(
                    est.getId(),
                    est.getName(),
                    est.getAddress(),
                    est.getRating(),
                    est.getType(),
                    photo
            ));
        }
        log.info("--- [SERVICE] getFavoriteListDto: Found {} favorite DTOs for userId={}", dtos.size(), userId);
        return dtos;
    }

    // 5. НОВЫЙ: Проверить, в избранном ли заведение (для Android checkFavorite)
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long establishmentId) {
        log.info("--- [SERVICE] isFavorite: Received userId={}, establishmentId={}", userId, establishmentId);
        AccountEntity user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        boolean result = user.getFavorites().stream()
                .anyMatch(est -> est.getId().equals(establishmentId));
        log.info("--- [SERVICE] isFavorite: Result={} for userId={}, establishmentId={}", result, userId, establishmentId);
        return result;
    }
}
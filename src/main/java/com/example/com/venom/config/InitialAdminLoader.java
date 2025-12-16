package com.example.com.venom.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.entity.UserEntity.Role;
import com.example.com.venom.repository.UserRepository;

@Component
public class InitialAdminLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public InitialAdminLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Создаем первого администратора
        createUserIfNotExists("mrPedick", "mrPedick", "5422f8aa", Role.AdminOfApp, 1L);

        // Создаем второго администратора для отзывов
        createUserIfNotExists("Reviewer Admin", "reviewer2", "reviewerPass", Role.AdminOfApp, 2L);

        System.out.println("✅ Проверка администраторов завершена");
    }

    private void createUserIfNotExists(String name, String login, String password, Role role, Long desiredId) {
        Optional<UserEntity> existingUser = userRepository.findByLogin(login);

        if (existingUser.isEmpty()) {
            UserEntity user = new UserEntity(name, login, password, role);

            // Для тестовых данных можем задать ID напрямую
            // ВНИМАНИЕ: Это работает только если в сущности UserEntity есть возможность задать ID
            try {
                // Если в UserEntity есть setId метод
                user.setId(desiredId);
                System.out.println(">>> Создан администратор: " + login + " с ID: " + desiredId);
            } catch (Exception e) {
                System.out.println(">>> Создан администратор: " + login + " (ID сгенерируется автоматически)");
            }

            userRepository.save(user);
        } else {
            System.out.println("ℹ>>> Администратор " + login + " уже существует");
        }
    }
}
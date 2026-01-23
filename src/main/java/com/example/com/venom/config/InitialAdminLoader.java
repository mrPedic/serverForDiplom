package com.example.com.venom.config;

import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;  // Добавь импорт, если Spring Security
import org.springframework.stereotype.Component;

import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.entity.UserEntity.Role;
import com.example.com.venom.repository.UserRepository;

@Component
@Order(1)
public class InitialAdminLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Внедри, если нужно кодирование

    public InitialAdminLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Создаем первого администратора
        createUserIfNotExists("mrPedick", "mrPedick", "5422f8aa", Role.AdminOfApp);

        // Создаем второго администратора для отзывов
        createUserIfNotExists("Reviewer Admin", "reviewer2", "reviewerPass", Role.AdminOfApp);

        System.out.println("✅ Проверка администраторов завершена");
    }

    @Transactional
    private void createUserIfNotExists(String name, String login, String rawPassword, Role role) {
        Optional<UserEntity> existingUser = userRepository.findByLogin(login);

        if (existingUser.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(rawPassword);  // Кодируем пароль
            UserEntity user = new UserEntity(name, login, encodedPassword, role);

            // НЕ устанавливаем ID вручную — пусть БД генерирует
            user = userRepository.save(user);  // Save и получаем entity с ID

            System.out.println(">>> Создан администратор: " + login + " с ID: " + user.getId());
        } else {
            System.out.println("ℹ>>> Администратор " + login + " уже существует");
        }
    }
}
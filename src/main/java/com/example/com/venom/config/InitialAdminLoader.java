package com.example.com.venom.config; // Создайте новый пакет для конфигурации

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.com.venom.entity.AccountEntity;
import com.example.com.venom.entity.AccountEntity.Role;
import com.example.com.venom.repository.AccountRepository;

@Component
public class InitialAdminLoader implements CommandLineRunner {

    private final AccountRepository accountRepository;

    public InitialAdminLoader(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Попытка найти аккаунт с логином "admin"
        Optional<AccountEntity> existingAdmin = accountRepository.findByLogin("mrPedick");

        if (existingAdmin.isEmpty()) {
            // Создание аккаунта админа
            AccountEntity admin = new AccountEntity(
                "mrPedick", // Имя
                "mrPedick",     // Логин
                "5422f8aa", // Пароль (используйте хеширование в реальном приложении!)
                Role.AdminOfApp  // Роль
            );
            
            // Если вы хотите ИМЕННО id=0, то:
            // 1. Установите id. В AccountEntity добавьте public AccountEntity(Long id, ...) 
            //    или явно вызовите admin.setId(0L); 
            // 2. Убедитесь, что ваша БД позволяет явную вставку ID при GenerationType.IDENTITY.
            //    (В PostgreSQL, например, это требует дополнительных манипуляций с SEQUENCE).
            
            // Внимание: В большинстве БД (особенно с GenerationType.IDENTITY)
            // нельзя просто так задать ID, он генерируется. 
            // Для упрощения, лучше позволить БД сгенерировать ID для "admin", 
            // а не жестко задавать '0', если только '0' не критично.
            
            // Если '0' *обязательно* и вы уверены, что БД это позволяет:
            
            accountRepository.save(admin);
            System.out.println(">>> Создан начальный аккаунт администратора с логином: admin");
        } else {
            System.out.println("ℹ>>> Аккаунт администратора уже существует. Пропуск инициализации.");
        }
    }
}
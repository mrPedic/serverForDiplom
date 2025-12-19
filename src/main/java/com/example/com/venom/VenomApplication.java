package com.example.com.venom;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;


@SpringBootApplication(scanBasePackages = {"com.example.com.venom", "com.example.websocket"})
@EntityScan("com.example.com.venom.entity")
@EnableJpaRepositories("com.example.com.venom.repository")
public class VenomApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenomApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(DataInitializationService dataInitializationService) {
        return args -> {
            System.out.println("\n🚀 Начало инициализации тестовых данных...");
            dataInitializationService.initializeData();
            System.out.println("✅ Тестовые данные успешно созданы!");
            System.out.println("   - 100 заведений с меню и столиками");
            System.out.println("   - 10-15 отзывов на каждое заведение");
            System.out.println("   - Все отзывы от пользователя с ID=2");
            System.out.println("   - Два администратора (ID=1 и ID=2)");
        };
    }

    @Component
    @ConditionalOnProperty(name = "venom.mode", havingValue = "global")
    public static class ServerInfoPrinter implements ApplicationListener<ApplicationReadyEvent> {

        private final DataInitializationService dataInitializationService;

        public ServerInfoPrinter(DataInitializationService dataInitializationService) {
            this.dataInitializationService = dataInitializationService;
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            try {
                System.out.println("\n✅ Сервер запущен в ГЛОБАЛЬНОМ режиме!");

                // Вызов Python-скрипта
                System.out.println("Вызов скрипта для настройки ngrok...");
                runPythonScript();

            } catch (Exception e) {
                System.err.println("❌ Ошибка при получении информации о сервере: " + e.getMessage());
            }
        }

        private void runPythonScript() {
            try {
                ProcessBuilder pb = new ProcessBuilder("python", "update_ngrok_gist.py");
                pb.directory(new File("src/main/resources/scripts"));
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    System.out.println("✅ Python-скрипт выполнен успешно. (ГЛОБАЛЬНЫЙ режим)");
                } else {
                    System.err.println("❌ Python-скрипт завершился с ошибкой. Код: " + exitCode);
                }
            } catch (Exception e) {
                System.err.println("❌ Ошибка при запуске Python-скрипта: " + e.getMessage());
            }
        }
    }
}
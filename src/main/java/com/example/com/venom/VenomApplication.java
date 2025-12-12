package com.example.com.venom;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EntityScan("com.example.com.venom.entity")
@EnableJpaRepositories("com.example.com.venom.repository")
public class VenomApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenomApplication.class, args);
        
    }

    @Component
    @ConditionalOnProperty(name = "venom.mode", havingValue = "global")
    public static class ServerInfoPrinter implements ApplicationListener<WebServerInitializedEvent> {
        
        @Override
        public void onApplicationEvent(WebServerInitializedEvent event) {
            try {
                // Измененный вывод, чтобы было понятно, какой режим
                System.out.println("\n✅ Сервер запущен в ГЛОБАЛЬНОМ режиме!");
                System.out.println("Вызов скрипта для настройки ngrok...");

                // 👉 Вызов Python-скрипта
                runPythonScript();
                
            } catch (Exception e) {
                System.err.println("❌ Ошибка при получении информации о сервере: " + e.getMessage());
            }
        }

        private void runPythonScript() {
            try {
                // Путь к твоему скрипту (если он в одной папке с jar)
                ProcessBuilder pb = new ProcessBuilder("python", "update_ngrok_gist.py");

                // 👉 Укажи путь к папке, где лежит скрипт
                // ОСТОРОЖНО: Это работает, если запускать из IDE. Для jar-файла нужен другой путь.
                pb.directory(new File("src/main/resources/scripts")); 

                // Наследовать вывод в консоль
                pb.inheritIO();

                // Запуск
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
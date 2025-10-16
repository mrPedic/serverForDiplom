package com.example.com.venom.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Конфигурация Jackson для корректной работы с Java 8 Date/Time API (java.time.*).
 *
 * Регистрирует JavaTimeModule и отключает запись дат как временных меток (Timestamp)
 * в пользу ISO-8601 строк (например, "2025-10-16").
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizeObjectMapper() {
        return builder -> {
            // Регистрируем модуль для поддержки типов java.time.*
            builder.modules(new JavaTimeModule());
            
            // Отключаем сериализацию дат как Unix Timestamp, 
            // используя стандартный ISO-8601 формат.
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}

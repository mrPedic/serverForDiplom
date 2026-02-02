package com.example.com.venom.service;

import com.example.com.venom.entity.AdminQueryEntity;
import com.example.com.venom.repository.AdminQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSqlService {

    private final AdminQueryRepository queryRepository;
    private final JdbcTemplate jdbcTemplate;

    // --- Методы для управления сохраненными запросами ---

    public List<AdminQueryEntity> getAllSavedQueries() {
        return queryRepository.findAll();
    }

    public AdminQueryEntity saveQuery(AdminQueryEntity entity) {
        if (entity.getId() != null && entity.getId() == 0) {
            // Создаем абсолютно новый объект, чтобы Hibernate гарантированно сделал INSERT
            AdminQueryEntity newEntity = new AdminQueryEntity();
            newEntity.setTitle(entity.getTitle());
            newEntity.setDescription(entity.getDescription());
            newEntity.setSqlQuery(entity.getSqlQuery());
            // ID не устанавливаем, он будет null и сгенерируется базой
            return queryRepository.save(newEntity);
        }
        return queryRepository.save(entity);
    }

    public void deleteQuery(Long id) {
        queryRepository.deleteById(id);
    }

    // --- Метод выполнения сырого SQL ---

    @Transactional
    public List<Map<String, Object>> executeRawSql(String sql) {
        String trimmedSql = sql.trim();
        log.info("Executing Admin SQL: {}", trimmedSql);

        try {
            // Определяем тип запроса
            if (isSelectQuery(trimmedSql)) {
                // Для SELECT возвращаем данные
                return jdbcTemplate.queryForList(trimmedSql);
            } else {
                // Для INSERT, UPDATE, DELETE возвращаем количество затронутых строк
                int rowsAffected = jdbcTemplate.update(trimmedSql);
                return List.of(Map.of(
                        "STATUS", "SUCCESS",
                        "ACTION", "UPDATE/INSERT/DELETE",
                        "ROWS_AFFECTED", rowsAffected
                ));
            }
        } catch (Exception e) {
            log.error("Error executing SQL: {}", e.getMessage());
            // Возвращаем ошибку в формате, который фронтенд сможет отобразить в таблице
            return List.of(Map.of(
                    "STATUS", "ERROR",
                    "MESSAGE", e.getMessage() != null ? e.getMessage() : "Unknown Error"
            ));
        }
    }

    private boolean isSelectQuery(String sql) {
        return sql.toUpperCase().startsWith("SELECT");
    }
}
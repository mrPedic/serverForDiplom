package com.example.com.venom.controller;

import com.example.com.venom.entity.AdminQueryEntity;
import com.example.com.venom.service.AdminSqlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sql")
@RequiredArgsConstructor
public class AdminSqlController {

    private final AdminSqlService adminSqlService;

    // ================== Управление сохраненными запросами ==================

    @GetMapping("/queries")
    public ResponseEntity<List<AdminQueryEntity>> getAllQueries() {
        return ResponseEntity.ok(adminSqlService.getAllSavedQueries());
    }

    @PostMapping("/queries")
    public ResponseEntity<AdminQueryEntity> saveQuery(@RequestBody AdminQueryEntity entity) {
        return ResponseEntity.ok(adminSqlService.saveQuery(entity));
    }

    @DeleteMapping("/queries/{id}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long id) {
        adminSqlService.deleteQuery(id);
        return ResponseEntity.ok().build();
    }

    // ================== Выполнение SQL ==================

    /**
     * Принимает SQL-запрос как простую строку (text/plain или application/json body).
     * Возвращает результат в виде списка Map, который легко отобразить в таблице на клиенте.
     */
    @PostMapping("/execute")
    public ResponseEntity<List<Map<String, Object>>> executeSql(@RequestBody String sql) {
        // 1. Убираем пробелы по краям
        String cleanSql = sql.trim();

        // 2. Если строка пришла в кавычках (как JSON string), убираем их
        if (cleanSql.startsWith("\"") && cleanSql.endsWith("\"") && cleanSql.length() > 1) {
            cleanSql = cleanSql.substring(1, cleanSql.length() - 1);
        }

        // 3. Убираем экранирование кавычек ( \" -> " )
        cleanSql = cleanSql.replace("\\\"", "\"");

        // 4. Заменяем экранированные переносы строк на пробелы (чтобы SQL был валидным одной строкой)
        cleanSql = cleanSql.replace("\\n", " ");

        return ResponseEntity.ok(adminSqlService.executeRawSql(cleanSql));
    }
}
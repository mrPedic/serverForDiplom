package com.example.com.venom.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.TableCreationDto;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.service.TableService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tables") 
@RequiredArgsConstructor
public class TableController {

    private static final Logger log = LoggerFactory.getLogger(TableController.class);
    
    private final TableService tableService; // Внедрение сервиса

    /**
     * Эндпойнт для создания списка столиков и привязки их к заведению.
     * * @param establishmentId ID заведения, к которому привязываем столики.
     * @param tableDtos Список столиков, полученный в теле запроса.
     * @return Список созданных столиков.
     */
    @PostMapping("/establishment/{establishmentId}/create") 
    public ResponseEntity<List<TableEntity>> createTables(
        @PathVariable Long establishmentId,
        @RequestBody List<TableCreationDto> tableDtos
    ) {
        log.info("--- [POST /tables/establishment/{}] Received {} tables for creation.", 
                 establishmentId, tableDtos != null ? tableDtos.size() : 0);
        
        if (establishmentId == null || establishmentId <= 0) {
            return ResponseEntity.badRequest().body(null); // ID заведения некорректен
        }

        if (tableDtos == null || tableDtos.isEmpty()) {
            // Технически это не ошибка, но можно предупредить
            log.warn("--- [POST /tables/establishment/{}] No tables provided in request body.", establishmentId);
            return ResponseEntity.ok(List.of()); 
        }

        // Сохраняем столики через сервис
        List<TableEntity> savedTables = tableService.createTablesForEstablishment(establishmentId, tableDtos);

        log.info("--- [POST /tables/establishment/{}] Successfully created {} table s.", 
                 establishmentId, savedTables.size());

        // Возвращаем клиенту созданные сущности (включая их новые ID)
        return ResponseEntity.ok(savedTables);
    }
}
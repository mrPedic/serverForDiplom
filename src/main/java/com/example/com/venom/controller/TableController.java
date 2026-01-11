package com.example.com.venom.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.com.venom.dto.establishment.TableCreationDto;
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
     * @param establishmentId ID заведения, к которому привязываем столики.
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

        log.info("--- [POST /tables/establishment/{}] Successfully created {} tables.", 
                 establishmentId, savedTables.size());

        // Возвращаем клиенту созданные сущности (включая их новые ID)
        return ResponseEntity.ok(savedTables);
    }

    /**
     * Эндпойнт для получения всех столиков, принадлежащих конкретному заведению.
     * Соответствует GET /tables/establishment/{establishmentId} в ApiService.
     * @param establishmentId ID заведения.
     * @return Список столиков.
     */
    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<TableEntity>> getTablesByEstablishmentId(@PathVariable Long establishmentId) {
        log.info("--- [GET /tables/establishment/{}] Request received for all tables.", establishmentId);

        if (establishmentId == null || establishmentId <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        List<TableEntity> tables = tableService.findAllByEstablishmentId(establishmentId);

        log.info("--- [GET /tables/establishment/{}] Found {} tables.", establishmentId, tables.size());
        return ResponseEntity.ok(tables);
    }

    /**
     * Эндпойнт для удаления столика по его ID.
     * Соответствует DELETE /tables/{id} в ApiService.
     * @param id ID столика для удаления.
     * @return 204 No Content в случае успеха или 404/500 в случае ошибки.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        log.info("--- [DELETE /tables/{}] Request received for deletion.", id);

        if (id == null || id <= 0) {
            log.warn("--- [DELETE /tables/{}] Invalid table ID provided.", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            tableService.deleteTable(id);
            log.info("--- [DELETE /tables/{}] Table successfully deleted.", id);
            return ResponseEntity.noContent().build(); // 204 No Content - стандартный ответ для успешного удаления
        } catch (Exception e) {
            log.error("--- [DELETE /tables/{}] Error deleting table: {}", id, e.getMessage(), e);
            // Возвращаем 500 Internal Server Error, если что-то пошло не так на уровне сервиса
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
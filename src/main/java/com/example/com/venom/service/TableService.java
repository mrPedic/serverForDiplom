package com.example.com.venom.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.com.venom.dto.TableCreationDto;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.repository.TableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    /**
     * Сохраняет список столиков, привязывая их к указанному заведению.
     * @param establishmentId ID заведения, которому принадлежат столики.
     * @param tableDtos Список DTO столиков, полученный с клиента.
     * @return Список сохраненных сущностей столиков.
     */
    public List<TableEntity> createTablesForEstablishment(Long establishmentId, List<TableCreationDto> tableDtos) {
        if (tableDtos == null || tableDtos.isEmpty()) {
            return List.of(); // Возвращаем пустой список, если нет данных
        }

        List<TableEntity> savedTables = new ArrayList<>();

        for (TableCreationDto tableDto : tableDtos) {
            TableEntity tableEntity = new TableEntity();
            
            // ⭐ КЛЮЧЕВАЯ ПРИВЯЗКА:
            tableEntity.setEstablishmentId(establishmentId); 
            
            // Маппинг данных
            tableEntity.setName(tableDto.getName());
            tableEntity.setDescription(tableDto.getDescription());
            tableEntity.setMaxCapacity(tableDto.getMaxCapacity());
            
            savedTables.add(tableRepository.save(tableEntity));
        }

        return savedTables;
    }
    
    /**
     * ⭐ ДОБАВЛЕНО: Возвращает все столики, привязанные к указанному заведению.
     * Используется для отображения всех столиков в панели управления заведения.
     * @param establishmentId ID заведения.
     * @return Список всех столиков заведения.
     */
    public List<TableEntity> findAllByEstablishmentId(Long establishmentId) {
        return tableRepository.findByEstablishmentId(establishmentId);
    }

    /**
     * Возвращает список столиков, доступных для бронирования
     * в указанном заведении на указанное время.
     * * ВНИМАНИЕ: Здесь должна быть реальная логика проверки занятости, 
     * которая обращается к таблице бронирований. 
     * * @param establishmentId ID заведения.
     * @param dateTime Время начала бронирования (ISO 8601).
     * @return Список доступных столиков.
     */
    public List<TableEntity> getAvailableTables(Long establishmentId, LocalDateTime dateTime) {
        // Шаг 1: Получаем все столики заведения
        List<TableEntity> allTables = tableRepository.findByEstablishmentId(establishmentId);

        // Шаг 2: Реализация логики проверки доступности
        
        // ⭐ ЗАГЛУШКА БЕЗ ПРОВЕРКИ БРОНИРОВАНИЯ:
        System.out.println("--- [TableService] Finding all tables for estId: " + establishmentId);
        
        return allTables;
        
        /* // ⭐ ПРИМЕР РЕАЛЬНОЙ ЛОГИКИ (если BookingRepository внедрен):
        // List<Long> reservedTableIds = bookingRepository.findReservedTableIds(establishmentId, dateTime);
        
        // return allTables.stream()
        //     .filter(table -> !reservedTableIds.contains(table.getId()))
        //     .collect(Collectors.toList());
        */
    }
    
    /**
     * ⭐ ДОБАВЛЕНО: Удаляет столик по его уникальному идентификатору.
     * @param id ID столика для удаления.
     */
    public void deleteTable(Long id) {
        // Spring Data JPA выполняет проверку существования ID внутри deleteById.
        // Если ID не существует, будет выброшено исключение EmptyResultDataAccessException, 
        // которое, при необходимости, можно перехватить в контроллере.
        tableRepository.deleteById(id);
    }
}
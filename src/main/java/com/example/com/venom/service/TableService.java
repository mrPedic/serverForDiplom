package com.example.com.venom.service;

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
}
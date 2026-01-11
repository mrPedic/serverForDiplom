package com.example.com.venom.service.order;

import com.example.com.venom.dto.order.TimeSlotDto;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TimeSlotGenerator {

    private final OrderRepository orderRepository;

    public TimeSlotGenerator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<TimeSlotDto> generateTimeSlots(
            EstablishmentEntity establishment,
            LocalDate date,
            int intervalMinutes,
            int stepMinutes) {

        List<TimeSlotDto> slots = new ArrayList<>();
        Map.Entry<LocalTime, LocalTime> workingHours = getWorkingHours(establishment, date);

        if (workingHours == null) {
            return slots;
        }

        LocalTime current = workingHours.getKey();
        LocalTime end = workingHours.getValue();
        LocalDateTime now = LocalDateTime.now();

        while (current.isBefore(end.minusMinutes(intervalMinutes))) {
            LocalTime slotEnd = current.plusMinutes(intervalMinutes);
            LocalDateTime slotStartDateTime = LocalDateTime.of(date, current);
            LocalDateTime slotEndDateTime = LocalDateTime.of(date, slotEnd);

            // Слот должен быть минимум через 45 минут от текущего времени
            boolean isAvailable = slotStartDateTime.isAfter(now.plusMinutes(45)) &&
                    isSlotAvailable(establishment.getId(), slotStartDateTime, slotEndDateTime);

            TimeSlotDto slot = new TimeSlotDto(
                    slotStartDateTime,
                    slotEndDateTime,
                    formatTimeSlot(current, slotEnd),
                    isAvailable
            );

            slots.add(slot);
            current = current.plusMinutes(stepMinutes);
        }

        return slots;
    }

    private String formatTimeSlot(LocalTime start, LocalTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return start.format(formatter) + " - " + end.format(formatter);
    }

    private Map.Entry<LocalTime, LocalTime> getWorkingHours(EstablishmentEntity establishment, LocalDate date) {
        String operatingHours = establishment.getOperatingHoursString();
        if (operatingHours == null || operatingHours.trim().isEmpty()) {
            // Возвращаем стандартные часы работы, если не указаны
            return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
        }

        // Конвертируем день недели в русское название
        String[] days = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        String dayName = days[date.getDayOfWeek().getValue() - 1];

        // Разбираем строку расписания
        String[] parts = operatingHours.split(", ");
        for (String part : parts) {
            if (part.contains(dayName)) {
                // Нашли часть расписания для нашего дня
                String timePart = part.substring(part.indexOf(" ") + 1);
                String[] times = timePart.split("-");
                if (times.length == 2) {
                    try {
                        LocalTime startTime = LocalTime.parse(times[0]);
                        LocalTime endTime = LocalTime.parse(times[1]);
                        return new AbstractMap.SimpleEntry<>(startTime, endTime);
                    } catch (Exception e) {
                        // Неверный формат времени, возвращаем стандартные часы
                        return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
                    }
                }
            }
        }

        // Если день не найден, возвращаем стандартные часы
        return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
    }

    private boolean isSlotAvailable(Long establishmentId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        // Проверка количества заказов в этот временной слот
        long orderCount = orderRepository.countOrdersInTimeSlot(
                establishmentId,
                slotStart,
                slotEnd
        );

        // Максимально 3 заказа в один временной слот
        return orderCount < 3;
    }
}
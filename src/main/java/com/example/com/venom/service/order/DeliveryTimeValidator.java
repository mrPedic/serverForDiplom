package com.example.com.venom.service.order;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.Map;

@Service
public class DeliveryTimeValidator {

    private final OrderRepository orderRepository;

    public DeliveryTimeValidator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void validate(EstablishmentEntity establishment, LocalDateTime deliveryTime) {
        // 1. Проверка, что время в будущем (минимум через 45 минут)
        LocalDateTime minTime = LocalDateTime.now().plusMinutes(45);
        if (deliveryTime.isBefore(minTime)) {
            throw new IllegalArgumentException("Минимальное время доставки - 45 минут от текущего времени");
        }

        // 2. Проверка времени работы заведения
        DayOfWeek dayOfWeek = deliveryTime.getDayOfWeek();
        String operatingHours = establishment.getOperatingHoursString();

        Map.Entry<LocalTime, LocalTime> workingHours = parseWorkingHours(operatingHours, dayOfWeek);
        if (workingHours == null) {
            throw new IllegalArgumentException("Заведение не работает в выбранный день");
        }

        LocalTime deliveryLocalTime = deliveryTime.toLocalTime();
        if (deliveryLocalTime.isBefore(workingHours.getKey()) ||
                deliveryLocalTime.isAfter(workingHours.getValue().minusMinutes(30))) {
            throw new IllegalArgumentException("Время доставки выходит за рамки времени работы заведения");
        }

        // 3. Проверка, что заведение может принять заказ на это время
        // (не превышен лимит одновременных заказов)
        if (!isTimeSlotAvailable(establishment.getId(), deliveryTime)) {
            throw new IllegalArgumentException("Выбранное время недоступно. Пожалуйста, выберите другое время");
        }
    }

    private boolean isTimeSlotAvailable(Long establishmentId, LocalDateTime deliveryTime) {
        // Проверка количества заказов в этот временной слот
        LocalDateTime slotStart = deliveryTime.minusMinutes(15);
        LocalDateTime slotEnd = deliveryTime.plusMinutes(45);

        long orderCount = orderRepository.countOrdersInTimeSlot(
                establishmentId,
                slotStart,
                slotEnd
        );

        // Максимально 3 заказа в один временной слот
        return orderCount < 3;
    }

    private Map.Entry<LocalTime, LocalTime> parseWorkingHours(String operatingHours, DayOfWeek dayOfWeek) {
        if (operatingHours == null || operatingHours.trim().isEmpty()) {
            return null;
        }

        // Конвертируем день недели в русское название
        String[] days = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        String dayName = days[dayOfWeek.getValue() - 1];

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
                        // Неверный формат времени
                        return null;
                    }
                }
            }
        }

        return null;
    }
}
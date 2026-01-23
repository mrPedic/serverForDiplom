package com.example.com.venom.service.order;

import com.example.com.venom.dto.order.TimeSlotDto;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TimeSlotGenerator {

    private final OrderRepository orderRepository;
    private final EstablishmentRepository establishmentRepository;

    // Маппинг русских сокращений → DayOfWeek
    private static final Map<String, DayOfWeek> RUSSIAN_DAY_MAP = Map.of(
            "Пн", DayOfWeek.MONDAY,
            "Вт", DayOfWeek.TUESDAY,
            "Ср", DayOfWeek.WEDNESDAY,
            "Чт", DayOfWeek.THURSDAY,
            "Пт", DayOfWeek.FRIDAY,
            "Сб", DayOfWeek.SATURDAY,
            "Вс", DayOfWeek.SUNDAY
    );

    // Обратный маппинг для поиска
    private static final Map<DayOfWeek, String> RUSSIAN_SHORT = RUSSIAN_DAY_MAP.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public TimeSlotGenerator(OrderRepository orderRepository, EstablishmentRepository establishmentRepository) {
        this.orderRepository = orderRepository;
        this.establishmentRepository = establishmentRepository;
    }

    /**
     * Генерирует доступные временные слоты для заведения на указанную дату
     */
    public List<TimeSlotDto> generateTimeSlots(Long establishmentId, LocalDate date) {
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new IllegalArgumentException("Заведение не найдено"));

        // Получаем рабочие часы для указанного дня
        Map.Entry<LocalTime, LocalTime> workingHours = getWorkingHoursForDate(establishment, date);
        if (workingHours == null) {
            return Collections.emptyList(); // Заведение не работает в этот день
        }

        LocalTime openingTime = workingHours.getKey();
        LocalTime closingTime = workingHours.getValue();

        List<TimeSlotDto> slots = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalTime minStartTime = now.toLocalTime().plusMinutes(45);
        if (minStartTime.isBefore(openingTime)) {
            minStartTime = openingTime;
        }

        // Округляем до ближайшего 15-минутного интервала
        minStartTime = roundToNext15Minutes(minStartTime);

        LocalDateTime current = LocalDateTime.of(date, minStartTime);

        while (current.toLocalTime().plusMinutes(30).isBefore(closingTime)) {
            LocalDateTime slotStart = current;
            LocalDateTime slotEnd = slotStart.plusMinutes(30);

            boolean available = isSlotAvailable(establishmentId, slotStart, slotEnd);

            TimeSlotDto slot = new TimeSlotDto();
            slot.setStartTime(slotStart);
            slot.setEndTime(slotEnd);
            slot.setAvailable(available);
            slots.add(slot);

            current = current.plusMinutes(15); // шаг 15 минут
        }

        return slots;
    }

    /**
     * Возвращает рабочие часы заведения для конкретной даты
     */
    private Map.Entry<LocalTime, LocalTime> getWorkingHoursForDate(EstablishmentEntity establishment, LocalDate date) {
        String hoursStr = establishment.getOperatingHoursString();  // ← правильный геттер
        if (hoursStr == null || hoursStr.trim().isEmpty()) {
            return null; // Заведение не работает
        }

        Map<String, String> hoursMap = parseOperatingHours(hoursStr);

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 1. Пытаемся найти по русскому сокращению
        String russianKey = RUSSIAN_SHORT.get(dayOfWeek);
        String hours = hoursMap.get(russianKey);

        // 2. Если не нашли — пробуем английское название
        if (hours == null) {
            String englishKey = dayOfWeek.name();
            hours = hoursMap.get(englishKey);
        }

        if (hours == null) {
            return null; // День не найден → заведение не работает
        }

        return parseTimeRange(hours);
    }

    /**
     * Парсит строку вида ["Пн: 10:00-20:00", "Вт: ..."] в Map<День, "HH:mm-HH:mm">
     */
    private Map<String, String> parseOperatingHours(String hoursStr) {
        Map<String, String> result = new LinkedHashMap<>();

        if (hoursStr == null || hoursStr.trim().isEmpty()) {
            return result;
        }

        // Убираем квадратные скобки, если есть
        String cleaned = hoursStr.replaceAll("^\\[|\\]$", "").trim();

        // Разбиваем по запятым (учитывая кавычки)
        Pattern entryPattern = Pattern.compile("\"?([^\"]+):\\s*([^\"]+)\"?");
        Matcher matcher = entryPattern.matcher(cleaned);

        while (matcher.find()) {
            String day = matcher.group(1).trim();
            String time = matcher.group(2).trim();
            result.put(day, time);
        }

        // Если ничего не нашлось — пробуем простой формат без кавычек
        if (result.isEmpty()) {
            String[] parts = cleaned.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;

                int colonIndex = part.indexOf(':');
                if (colonIndex > 0) {
                    String day = part.substring(0, colonIndex).trim();
                    String time = part.substring(colonIndex + 1).trim();
                    result.put(day, time);
                }
            }
        }

        return result;
    }

    /**
     * Парсит "10:00-20:00" в пару LocalTime
     */
    private Map.Entry<LocalTime, LocalTime> parseTimeRange(String timeRange) {
        if (timeRange == null || !timeRange.contains("-")) {
            return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
        }

        String[] times = timeRange.split("-");
        if (times.length != 2) {
            return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
        }

        try {
            LocalTime start = LocalTime.parse(times[0].trim());
            LocalTime end = LocalTime.parse(times[1].trim());
            return new AbstractMap.SimpleEntry<>(start, end);
        } catch (Exception e) {
            return new AbstractMap.SimpleEntry<>(LocalTime.of(9, 0), LocalTime.of(22, 0));
        }
    }

    /**
     * Округляет время до следующего 15-минутного интервала
     */
    private LocalTime roundToNext15Minutes(LocalTime time) {
        int minute = time.getMinute();
        int remainder = minute % 15;
        if (remainder == 0) {
            return time;
        }
        return time.plusMinutes(15 - remainder);
    }

    /**
     * Проверяет, доступен ли слот (меньше 3 заказов)
     */
    private boolean isSlotAvailable(Long establishmentId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        long orderCount = orderRepository.countOrdersInTimeSlot(
                establishmentId,
                slotStart,
                slotEnd
        );
        return orderCount < 3;
    }
}
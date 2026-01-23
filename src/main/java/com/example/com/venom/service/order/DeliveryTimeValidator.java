package com.example.com.venom.service.order;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.repository.order.OrderRepository;
import com.example.com.venom.service.BusinessException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DeliveryTimeValidator {

    private final OrderRepository orderRepository;

    // Маппинг русских сокращений и полных имен → DayOfWeek
    private static final Map<String, DayOfWeek> RUSSIAN_DAY_MAP = new HashMap<>();

    static {
        // Короткие
        RUSSIAN_DAY_MAP.put("Пн", DayOfWeek.MONDAY);
        RUSSIAN_DAY_MAP.put("Вт", DayOfWeek.TUESDAY);
        RUSSIAN_DAY_MAP.put("Ср", DayOfWeek.WEDNESDAY);
        RUSSIAN_DAY_MAP.put("Чт", DayOfWeek.THURSDAY);
        RUSSIAN_DAY_MAP.put("Пт", DayOfWeek.FRIDAY);
        RUSSIAN_DAY_MAP.put("Сб", DayOfWeek.SATURDAY);
        RUSSIAN_DAY_MAP.put("Вс", DayOfWeek.SUNDAY);
        // Полные
        RUSSIAN_DAY_MAP.put("Понедельник", DayOfWeek.MONDAY);
        RUSSIAN_DAY_MAP.put("Вторник", DayOfWeek.TUESDAY);
        RUSSIAN_DAY_MAP.put("Среда", DayOfWeek.WEDNESDAY);
        RUSSIAN_DAY_MAP.put("Четверг", DayOfWeek.THURSDAY);
        RUSSIAN_DAY_MAP.put("Пятница", DayOfWeek.FRIDAY);
        RUSSIAN_DAY_MAP.put("Суббота", DayOfWeek.SATURDAY);
        RUSSIAN_DAY_MAP.put("Воскресенье", DayOfWeek.SUNDAY);
    }

    // Список коротких русских дней в порядке для обработки диапазонов
    private static final List<String> RUSSIAN_SHORT_DAYS_ORDER = Arrays.asList("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс");

    public DeliveryTimeValidator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Основной метод валидации времени доставки
     */
    public void validate(EstablishmentEntity establishment, LocalDateTime deliveryTime) {
        if (establishment == null) {
            throw new IllegalArgumentException("Заведение не может быть null");
        }

        if (deliveryTime == null) {
            throw new IllegalArgumentException("Время доставки не указано");
        }

        LocalDateTime now = LocalDateTime.now();

        // Проверка: время доставки должно быть в будущем
        if (!deliveryTime.isAfter(now)) {
            throw new BusinessException("Время доставки должно быть в будущем");
        }

        // Проверка: минимум через 45 минут
        if (deliveryTime.isBefore(now.plusMinutes(45))) {
            throw new BusinessException("Время доставки должно быть минимум через 45 минут от текущего времени");
        }

        // Проверка: заведение работает в этот день
        DayOfWeek dayOfWeek = deliveryTime.toLocalDate().getDayOfWeek();
        Map<String, String> workingHours = parseOperatingHours(establishment.getOperatingHoursString());

        String hoursForDay = findHoursForDay(workingHours, dayOfWeek);

        if (hoursForDay == null || hoursForDay.equalsIgnoreCase("Закрыто")) {
            throw new BusinessException("Заведение не работает в выбранный день");
        }

        // Проверка: время доставки попадает в рабочие часы
        AbstractMap.SimpleEntry<LocalTime, LocalTime> timeRange = parseTimeRange(hoursForDay);
        if (timeRange == null) {
            throw new BusinessException("Некорректный формат рабочего времени заведения");
        }

        LocalTime open = timeRange.getKey();
        LocalTime close = timeRange.getValue();
        LocalTime deliveryLocalTime = deliveryTime.toLocalTime();

        boolean isOverMidnight = close.isBefore(open) || close.equals(LocalTime.MIDNIGHT);
        boolean isInside;

        if (isOverMidnight) {
            // Время работы переходит через полночь
            isInside = deliveryLocalTime.isAfter(open) || deliveryLocalTime.isBefore(close);
        } else {
            // Нормальное время работы
            isInside = !deliveryLocalTime.isBefore(open) && !deliveryLocalTime.isAfter(close);
        }

        if (!isInside) {
            throw new BusinessException("Время доставки вне рабочих часов заведения");
        }

        // Дополнительно: проверка загрузки слота (если нужно)
        // long count = orderRepository.countOrdersInTimeSlot(...);
        // if (count >= MAX_ORDERS_PER_SLOT) {
        //     throw new BusinessException("Временной слот заполнен");
        // }
    }

    /**
     * Ищет подходящую строку часов для дня недели
     * Сначала по русскому сокращению, потом по английскому названию
     */
    private String findHoursForDay(Map<String, String> hoursMap, DayOfWeek dayOfWeek) {
        // 1. Пробуем русский вариант (короткий, так как нормализуем к короткому)
        String russianKey = RUSSIAN_SHORT_DAYS_ORDER.get(dayOfWeek.getValue() - 1); // MONDAY=1 -> index 0 = "Пн"

        if (hoursMap.containsKey(russianKey)) {
            return hoursMap.get(russianKey);
        }

        // 2. Пробуем английский вариант
        String englishKey = dayOfWeek.name();
        if (hoursMap.containsKey(englishKey)) {
            return hoursMap.get(englishKey);
        }

        return null;
    }

    /**
     * Парсит строку operating_hours_str в Map<День(короткий), "HH:mm-HH:mm">
     * Теперь поддерживает диапазоны вроде "Пн-Вс: 10:00-20:00" или "Понедельник-Пятница: 10:00-20:00"
     */
    private Map<String, String> parseOperatingHours(String hoursStr) {
        Map<String, String> result = new HashMap<>();

        if (hoursStr == null || hoursStr.trim().isEmpty()) {
            return result;
        }

        // Убираем квадратные скобки, если они есть
        String cleaned = hoursStr.replaceAll("^\\[|\\]$", "").trim();

        // Паттерн для захвата дня/диапазона: "\"?([\\p{L}+-]+):\\s*([^\"]+)\"?"
        Pattern pattern = Pattern.compile("\"?([\\p{L}+-]+):\\s*([^\"]+)\"?");
        Matcher matcher = pattern.matcher(cleaned);

        while (matcher.find()) {
            String dayPart = matcher.group(1).trim();
            String timePart = matcher.group(2).trim();

            if (timePart.isEmpty() || timePart.equalsIgnoreCase("Закрыто")) {
                continue; // Пропускаем закрытые дни
            }

            // Нормализуем dayPart к короткому
            dayPart = normalizeDay(dayPart);

            // Если dayPart содержит "-", это диапазон
            if (dayPart.contains("-")) {
                String[] range = dayPart.split("-", 2);
                if (range.length == 2) {
                    String startDay = normalizeDay(range[0].trim());
                    String endDay = normalizeDay(range[1].trim());
                    applyRangeToMap(result, startDay, endDay, timePart);
                }
            } else {
                // Одиночный день
                result.put(dayPart, timePart);
            }
        }

        // Если ничего не распарсилось — пробуем простой формат без кавычек
        if (result.isEmpty()) {
            String[] parts = cleaned.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;

                int colonIndex = part.indexOf(':');
                if (colonIndex > 0) {
                    String dayPart = part.substring(0, colonIndex).trim();
                    String timePart = part.substring(colonIndex + 1).trim();

                    if (timePart.isEmpty() || timePart.equalsIgnoreCase("Закрыто")) {
                        continue;
                    }

                    dayPart = normalizeDay(dayPart);

                    if (dayPart.contains("-")) {
                        String[] range = dayPart.split("-", 2);
                        if (range.length == 2) {
                            String startDay = normalizeDay(range[0].trim());
                            String endDay = normalizeDay(range[1].trim());
                            applyRangeToMap(result, startDay, endDay, timePart);
                        }
                    } else {
                        result.put(dayPart, timePart);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Нормализует имя дня к короткому русскому варианту
     */
    private String normalizeDay(String day) {
        String lowerDay = day.toLowerCase(Locale.getDefault());
        for (Map.Entry<String, DayOfWeek> entry : RUSSIAN_DAY_MAP.entrySet()) {
            if (lowerDay.contains(entry.getKey().toLowerCase())) {
                // Если полное имя, возвращаем короткое
                switch (entry.getKey()) {
                    case "Понедельник":
                        return "Пн";
                    case "Вторник":
                        return "Вт";
                    case "Среда":
                        return "Ср";
                    case "Четверг":
                        return "Чт";
                    case "Пятница":
                        return "Пт";
                    case "Суббота":
                        return "Сб";
                    case "Воскресенье":
                        return "Вс";
                    default:
                        return entry.getKey();
                }
            }
        }
        return day; // Если не нашли, возвращаем как есть
    }

    /**
     * Применяет время к диапазону дней (например, "Пн-Вс")
     */
    private void applyRangeToMap(Map<String, String> map, String startDay, String endDay, String timePart) {
        int startIndex = RUSSIAN_SHORT_DAYS_ORDER.indexOf(startDay);
        int endIndex = RUSSIAN_SHORT_DAYS_ORDER.indexOf(endDay);

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            // Некорректный диапазон — пропускаем
            return;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            String day = RUSSIAN_SHORT_DAYS_ORDER.get(i);
            map.put(day, timePart);
        }
    }

    /**
     * Парсит строку "10:00-20:00" в пару LocalTime
     */
    private AbstractMap.SimpleEntry<LocalTime, LocalTime> parseTimeRange(String timeRange) {
        if (timeRange == null || !timeRange.contains("-")) {
            return null;
        }

        String[] times = timeRange.split("-");
        if (times.length != 2) {
            return null;
        }

        String startStr = times[0].trim();
        String endStr = times[1].trim();

        // Обработка форматов HH:mm, H:mm, HH:m, etc.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[H:m][HH:m][H:mm][HH:mm]");

        try {
            LocalTime start = LocalTime.parse(startStr, formatter);
            LocalTime end;
            if (endStr.equals("24:00") || endStr.equals("24") || endStr.equals("00:00") || endStr.equals("00")) {
                end = LocalTime.MIDNIGHT;
            } else {
                end = LocalTime.parse(endStr, formatter);
            }
            return new AbstractMap.SimpleEntry<>(start, end);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
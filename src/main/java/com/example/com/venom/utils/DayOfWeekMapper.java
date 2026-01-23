package com.example.com.venom.utils;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.stream.Collectors;

public final class DayOfWeekMapper {

    private static final Map<DayOfWeek, String> RUSSIAN_SHORT = Map.of(
            DayOfWeek.MONDAY,    "Пн",
            DayOfWeek.TUESDAY,   "Вт",
            DayOfWeek.WEDNESDAY, "Ср",
            DayOfWeek.THURSDAY,  "Чт",
            DayOfWeek.FRIDAY,    "Пт",
            DayOfWeek.SATURDAY,  "Сб",
            DayOfWeek.SUNDAY,    "Вс"
    );

    private static final Map<String, DayOfWeek> REVERSE_RUSSIAN =
            RUSSIAN_SHORT.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * Возвращает ключ для поиска в operating_hours_str
     * Сначала пытается найти русский вариант (Пн, Вт...),
     * если не нашёл — возвращает английский (Monday, Tuesday...)
     */
    public static String getKeyForHoursMap(DayOfWeek dayOfWeek) {
        return RUSSIAN_SHORT.getOrDefault(dayOfWeek, dayOfWeek.name());
    }

    /**
     * Обратное преобразование: из ключа (Пн или Monday) → DayOfWeek
     */
    public static DayOfWeek fromKey(String key) {
        if (key == null) return null;
        key = key.trim();

        // Пробуем русский
        DayOfWeek russian = REVERSE_RUSSIAN.get(key);
        if (russian != null) return russian;

        // Пробуем английский
        try {
            return DayOfWeek.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
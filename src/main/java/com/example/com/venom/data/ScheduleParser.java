package com.example.com.venom.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ScheduleParser {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static class Hours {
        public LocalTime open;
        public LocalTime close;

        public Hours(LocalTime open, LocalTime close) {
            this.open = open;
            this.close = close;
        }
    }

    private static final Map<String, Integer> DAY_MAP = new HashMap<>();
    static {
        DAY_MAP.put("Пн", 1); // Monday
        DAY_MAP.put("Вт", 2);
        DAY_MAP.put("Ср", 3);
        DAY_MAP.put("Чт", 4);
        DAY_MAP.put("Пт", 5);
        DAY_MAP.put("Сб", 6);
        DAY_MAP.put("Вс", 7);
    }

    /**
     * Парсит строку расписания и возвращает часы работы для указанной даты.
     * @param scheduleString Строка вроде "Пн-Сб 10:00-20:00, Вс 10:00-18:00"
     * @param date Дата для определения дня недели
     * @return Hours если найдено, иначе null (закрыто)
     */
    public static Hours getHoursForDay(String scheduleString, LocalDate date) {
        if (scheduleString == null || scheduleString.isEmpty()) {
            return null;
        }

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        String[] parts = scheduleString.split(",\\s*");
        for (String part : parts) {
            try {
                String[] dayTime = part.split("\\s+", 2);
                if (dayTime.length < 2) continue;

                String daysStr = dayTime[0].trim();
                String timeStr = dayTime[1].trim();

                String[] timeParts = timeStr.split("-");
                if (timeParts.length < 2) continue;

                String openStr = timeParts[0].trim();
                String closeStr = timeParts[1].trim();

                LocalTime openTime = parseTime(openStr);
                LocalTime closeTime = parseTime(closeStr);

                if (openTime == null || closeTime == null) continue;

                if (daysStr.contains("-")) {
                    String[] dayRange = daysStr.split("-");
                    if (dayRange.length < 2) continue;

                    String startDayStr = dayRange[0].trim();
                    String endDayStr = dayRange[1].trim();

                    Integer startDay = DAY_MAP.get(startDayStr);
                    Integer endDay = DAY_MAP.get(endDayStr);

                    if (startDay == null || endDay == null) continue;

                    boolean inRange = false;
                    if (startDay <= endDay) {
                        inRange = dayOfWeek >= startDay && dayOfWeek <= endDay;
                    } else {
                        // Редкий кейс wrap-around (например, Вс-Пн)
                        inRange = dayOfWeek >= startDay || dayOfWeek <= endDay;
                    }

                    if (inRange) {
                        return new Hours(openTime, closeTime);
                    }
                } else {
                    Integer singleDay = DAY_MAP.get(daysStr.trim());
                    if (singleDay != null && singleDay == dayOfWeek) {
                        return new Hours(openTime, closeTime);
                    }
                }
            } catch (Exception e) {
                // Пропускаем некорректную часть
                continue;
            }
        }

        return null; // Нет совпадения
    }

    private static LocalTime parseTime(String tStr) {
        if (tStr == null) return null;

        tStr = tStr.trim();
        if ("24:00".equals(tStr)) {
            return LocalTime.of(0, 0);
        }

        if (!tStr.contains(":")) {
            tStr += ":00";
        }

        // Добавляем 0 для однозначных часов (например, "8:00" -> "08:00")
        if (tStr.length() == 4 && tStr.charAt(1) == ':') {
            tStr = "0" + tStr;
        }

        try {
            return LocalTime.parse(tStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Проверяет, вписывается ли интервал брони в часы работы.
     * Учитывает midnight close (00:00) как до 23:59.
     * @param startTime Начало брони
     * @param endTime Конец брони
     * @param hours Часы работы
     * @return true если ок, false иначе
     */
    public static boolean isTimeWithinHours(LocalDateTime startTime, LocalDateTime endTime, Hours hours) {
        if (hours == null) {
            return false;
        }

        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();

        if (hours.close.equals(LocalTime.of(0, 0)) && !hours.open.equals(LocalTime.of(0, 0))) {
            // Close at midnight, allow up to 23:59
            LocalTime maxEnd = LocalTime.of(23, 59);
            return !start.isBefore(hours.open) && (!end.isAfter(maxEnd) || end.equals(LocalTime.of(0, 0))) && !start.isAfter(end);
        }

        return !start.isBefore(hours.open) && !end.isAfter(hours.close) && !start.isAfter(end);
    }
}
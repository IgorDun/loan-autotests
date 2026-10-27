package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Содержит методы для преобразования дат и времени.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_PATTERN_WITH_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Вычитает указанное количество дней от текущей даты и возвращает результат в виде строки.
     * @param date количество дней, которое нужно вычесть от текущей даты
     * @return строковое представление даты после вычитания указанного количества дней
     */
    public static String nowMinusDays(Long date) {
        return LocalDateTime.now().minusDays(date).toLocalDate().toString();
    }

    /**
     * Добавляет указанное количество дней к текущей дате и возвращает результат в виде строки.
     * @param date количество дней, которое нужно добавить к текущей дате
     * @return строковое представление даты после добавления указанного количества дней
     */
    public static String nowPlusDays(Long date) {
        return LocalDateTime.now().plusDays(date).toLocalDate().toString();
    }

    /**
     * Добавляет указанное количество минут к текущему времени и возвращает результат в указанном формате.
     * @param format формат времени, в котором нужно вернуть результат
     * @param minutes количество минут, которое нужно добавить к текущему времени
     * @return строковое представление времени после добавления указанного количества минут в указанном формате
     */
    public static String nowFormattedPlusMinutes(String format, Long minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now().plusMinutes(minutes));
    }

    /**
     * Добавляет указанное количество дней к текущей дате и возвращает результат в указанном формате времени.
     * @param days количество дней, которое нужно добавить к текущей дате
     * @param format формат времени, в котором нужно вернуть результат
     * @return строковое представление времени после добавления указанного количества дней в указанном формате
     */
    public static String nowFormattedPlusDays(Long days,String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now().plusDays(days));
    }

    /**
     * Вычитает указанное количество лет из текущей даты и возвращает результат в виде строки.
     * @param years количество лет, которое нужно вычесть из текущей даты
     * @return строковое представление даты после вычитания указанного количества лет
     */
    public static String nowMinusYears(long years) {
        return LocalDateTime.now().minusYears(years).toLocalDate().toString();
    }

    /**
     * Вычитает указанное количество дней из текущей даты, форматирует результат и возвращает его в указанном формате.
     * @param years количество дней, которое нужно вычесть из текущей даты
     * @param format формат времени, в котором нужно вернуть результат
     * @return отформатированное строковое представление даты после вычитания указанного количества дней
     */
    public static String nowMinusDaysFormatted(long years, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now().minusDays(years).toLocalDate());
    }

    /**
     * Возвращает текущую дату и время в указанном формате.
     * @param format формат времени, в котором нужно вернуть результат
     * @return отформатированное строковое представление текущей даты и времени
     */
    public static String nowFormatted(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now());
    }

    /**
     * Возвращает текущую дату в указанном формате.
     * @param format формат даты, в котором нужно вернуть результат
     * @return отформатированное строковое представление текущей даты
     */
    public static String nowDateFormatted(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now().toLocalDate());
    }

    /**
     * Вычитает указанное количество лет из текущей даты, форматирует результат и возвращает его в указанном формате.
     * @param years количество лет, которое нужно вычесть из текущей даты
     * @param format формат даты, в котором нужно вернуть результат
     * @return отформатированное строковое представление даты после вычитания указанного количества лет
     */
    public static String nowMinusYearsFormatted(long years, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(LocalDateTime.now().minusYears(years).toLocalDate());
    }

    /**
     * Возвращает текущую дату в формате строки.
     * @return строковое представление текущей даты
     */
    public static String now() {
        return LocalDateTime.now().toLocalDate().toString();
    }

    /**
     * Возвращает текущую дату и время в часовом поясе UTC.
     * @return объект LocalDateTime, представляющий текущую дату и время в часовом поясе UTC
     */
    public static LocalDateTime nowZoneTimeUTC() {
        return ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Возвращает текущую дату и время в указанном часовом поясе в формате строки.
     * @param zone идентификатор часового пояса, для которого нужно получить дату и время
     * @return строковое представление текущей даты и времени в указанном часовом поясе
     */
    public static String nowDateTimeWithZoneId(String zone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return formatter.format(ZonedDateTime.now(ZoneId.of(zone)));
    }

    /**
     * Возвращает текущую дату и время в формате строки, используя стандартный формат даты и времени.
     * @return строковое представление текущей даты и времени
     */
    public static String nowDateTime() {
        return nowFormatted(DATE_PATTERN);
    }

    /**
     * Возвращает текущую дату и время в формате строки, используя формат даты и времени с добавлением буквы "Z" в конце.
     * @return строковое представление текущей даты и времени с добавлением буквы "Z"
     */
    public static String nowDateTimeWithZ() {
        return nowFormatted(DATE_PATTERN_WITH_Z);
    }

    /**
     * Возвращает текущую дату и время, уменьшенные на указанное количество часов, в формате строки.
     * @param hours количество часов, которое нужно вычесть из текущего времени
     * @return строковое представление текущей даты и времени, уменьшенных на указанное количество часов
     */
    public static String nowDateTimeMinusHours(int hours) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return formatter.format(LocalDateTime.now().minusHours(hours));
    }
}

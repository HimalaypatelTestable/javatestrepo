package com.library.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DateUtils {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Set<DayOfWeek> WEEKEND = new HashSet<>(
            Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String formatIso(LocalDate date) {
        return date == null ? "" : date.format(ISO_FORMATTER);
    }

    public static String formatDisplay(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_FORMATTER);
    }

    public static String formatCompact(LocalDate date) {
        return date == null ? "" : date.format(COMPACT_FORMATTER);
    }

    public static LocalDate parseIso(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO date: " + text, e);
        }
    }

    public static LocalDate safeParseIso(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static int monthsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return (int) ChronoUnit.MONTHS.between(start, end);
    }

    public static int yearsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return Period.between(start, end).getYears();
    }

    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return WEEKEND.contains(date.getDayOfWeek());
    }

    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    public static LocalDate addBusinessDays(LocalDate from, int days) {
        if (from == null) {
            throw new IllegalArgumentException("From date cannot be null");
        }
        LocalDate result = from;
        int direction = days >= 0 ? 1 : -1;
        int remaining = Math.abs(days);
        while (remaining > 0) {
            result = result.plusDays(direction);
            if (isWeekday(result)) {
                remaining -= 1;
            }
        }
        return result;
    }

    public static long businessDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (end.isBefore(start)) {
            return -businessDaysBetween(end, start);
        }
        long count = 0;
        LocalDate cursor = start;
        while (cursor.isBefore(end)) {
            if (isWeekday(cursor)) {
                count += 1;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfMonth(1);
    }

    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate startOfYear(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return LocalDate.of(date.getYear(), Month.JANUARY, 1);
    }

    public static LocalDate endOfYear(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return LocalDate.of(date.getYear(), Month.DECEMBER, 31);
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static LocalDate min(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    public static LocalDate max(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    public static List<LocalDate> eachDay(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End cannot be before start");
        }
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    public static String humanize(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return "";
        }
        long days = Math.abs(daysBetween(from, to));
        if (days == 0) return "today";
        if (days == 1) return "1 day";
        if (days < 7) return days + " days";
        if (days < 30) return (days / 7) + " weeks";
        if (days < 365) return (days / 30) + " months";
        return (days / 365) + " years";
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public static int daysInMonth(int year, Month month) {
        return month.length(isLeapYear(year));
    }
}

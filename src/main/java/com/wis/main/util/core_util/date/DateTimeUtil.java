package com.wis.main.util.core_util.date;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.Locale;

public interface DateTimeUtil {
    default LocalDateTime nvl(LocalDateTime v, LocalDateTime nvl) {
        return v == null ? nvl : v;
    }

    String format(LocalDateTime v, String pattern);

    String format(LocalDate v, String pattern);

    <V> LocalDateTime toLocalDateTime(V v, String pattern);

    default LocalDateTime toLocalDateTime(LocalDate v) {
        return v == null ? null : v.atStartOfDay();
    }

    <V> LocalDateTime tryToLocalDateTime(V v, String pattern);

    <V> LocalDate toLocalDate(V v, String pattern);

    <V> LocalDate tryToLocalDate(V v, String pattern);

    default long nowMillis() {
        return millis(now());
    }

    default long millis(LocalDateTime v) {
        return v == null ? 0L : v.atZone(Zone.UTC).toInstant().toEpochMilli();
    }

    default LocalDateTime from(long millis) {
        return Instant.ofEpochMilli(millis).atZone(Zone.UTC).toLocalDateTime();
    }

    default LocalDateTime now() {
        return LocalDateTime.now(Clock.systemDefaultZone());
    }

    default LocalDateTime now(ZoneId zone) {
        return LocalDateTime.now(zone);
    }

    default LocalDate today() {
        return LocalDate.now(Zone.UTC);
    }

    default LocalDateTime atStartOfDay() {
        return atStartOfDay(now());
    }

    default LocalDateTime atStartOfDay(LocalDateTime v) {
        return v == null ? null : v.toLocalDate().atStartOfDay(Zone.UTC).toLocalDateTime();
    }

    default LocalDateTime atFirstDayOfMonth(LocalDateTime v) {
        return v == null ? null : v.withDayOfMonth(1);
    }

    default LocalDateTime atFirstDayOfNextMonth(LocalDateTime v) {
        LocalDateTime firstDayOfMonth = atFirstDayOfMonth(v);
        return plusMonths(firstDayOfMonth, 1L);
    }

    default LocalDateTime atEndOfMonth(LocalDateTime v) {
        LocalDateTime firstDayOfNextMonth = atFirstDayOfNextMonth(v);
        return plusDays(firstDayOfNextMonth, -1L);
    }

    default LocalDateTime atFirstDayOfYear(LocalDateTime v) {
        return v == null ? null : v.withDayOfYear(1);
    }

    default LocalDateTime atFirstDayOfWeek(LocalDateTime v) {
        return v == null ? null : v.with(WeekFields.of(Locale.CHINA).getFirstDayOfWeek());
    }

    default LocalDateTime atFirstDayOfQuarter(LocalDateTime v) {
        return v == null ? null : v.with(v.getMonth().firstMonthOfQuarter()).withDayOfMonth(1);
    }

    default LocalDateTime atQuarterHour(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        int hour = v.getHour();
        int div = hour / 4;
        hour = div * 4;
        v = atStartOfDay(v).withHour(hour);
        return v;
    }

    default LocalDateTime atHour(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        int hour = v.getHour();
        v = atHour(v, hour);
        return v;
    }

    default LocalDateTime atHour(LocalDateTime v, int hour) {
        if (v == null) {
            return null;
        }
        v = atStartOfDay(v).withHour(hour);
        return v;
    }

    default LocalDateTime atHalfHour(LocalDateTime v) {
        v = atMinute(v, 30);
        return v;
    }

    default LocalDateTime atQuarterMinute(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        int minute = v.getMinute();
        int div = minute / 15;
        minute = div * 15;
        v = atMinute(v, minute);
        return v;
    }

    default LocalDateTime atMinute(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        int minute = v.getMinute();
        v = atMinute(v, minute);
        return v;
    }

    default LocalDateTime atMinute(LocalDateTime v, int minute) {
        if (v == null) {
            return null;
        }
        v = atHour(v).withMinute(minute);
        return v;
    }

    default LocalDateTime atSecond(LocalDateTime v) {
        if (v == null) {
            return null;
        }
        int second = v.getSecond();
        v = atSecond(v, second);
        return v;
    }

    default LocalDateTime atSecond(LocalDateTime v, int second) {
        if (v == null) {
            return null;
        }
        v = atMinute(v).withSecond(second);
        return v;
    }

    default LocalDateTime plusYears(LocalDateTime v, long years) {
        return v == null ? null : v.plusYears(years);
    }

    default LocalDateTime plusWeeks(LocalDateTime v, long weeks) {
        return v == null ? null : v.plusWeeks(weeks);
    }

    default LocalDateTime plusMonths(LocalDateTime v, long months) {
        return v == null ? null : v.plusMonths(months);
    }

    default LocalDateTime plusDays(LocalDateTime v, long days) {
        return v == null ? null : v.plusDays(days);
    }

    default LocalDateTime plusHours(LocalDateTime v, long hours) {
        return v == null ? null : v.plusHours(hours);
    }

    default LocalDateTime plusMinutes(LocalDateTime v, long minutes) {
        return v == null ? null : v.plusMinutes(minutes);
    }

    default LocalDateTime plusSeconds(LocalDateTime v, long seconds) {
        return v == null ? null : v.plusSeconds(seconds);
    }

    default long daysBetween(LocalDateTime a, LocalDateTime b) {
        if (a == null || b == null) {
            return 0L;
        }
        return Duration.between(a, b).toDays();
    }

    default long hoursBetween(LocalDateTime a, LocalDateTime b) {
        if (a == null || b == null) {
            return 0L;
        }
        return Duration.between(a, b).toHours();
    }

    default long minutesBetween(LocalDateTime a, LocalDateTime b) {
        if (a == null || b == null) {
            return 0L;
        }
        return Duration.between(a, b).toMinutes();
    }

    default boolean isBefore(LocalDateTime a, LocalDateTime b) {
        return a != null && b != null && a.isBefore(b);
    }

    default boolean equals(LocalDateTime a, LocalDateTime b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    default boolean notEquals(LocalDateTime a, LocalDateTime b) {
        return !equals(a, b);
    }

    default boolean isBeforeOrEquals(LocalDateTime a, LocalDateTime b) {
        return isBefore(a, b) || equals(a, b);
    }

    default boolean isAfter(LocalDateTime a, LocalDateTime b) {
        return !isBeforeOrEquals(a, b);
    }

    default boolean isAfterOrEquals(LocalDateTime a, LocalDateTime b) {
        return isAfter(a, b) || equals(a, b);
    }

    default LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return isBefore(a, b) ? a : b;
    }

    default LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return isAfter(a, b) ? a : b;
    }
}

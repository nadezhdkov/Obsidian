/*
 * Copyright 2026 Rick M. Viana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package obsidian.experimental.time;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.io.Serial;

import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.time.*;
import java.util.*;

@SuppressWarnings("unused")
public final class DateTime implements Serializable, Comparable<DateTime> {

    @Serial
    private static final long serialVersionUID = 2L;

    private final ZonedDateTime zonedDateTime;

    // ==================== CONSTRUCTORS ====================

    private DateTime(@NotNull ZonedDateTime zonedDateTime) {
        this.zonedDateTime = Objects.requireNonNull(zonedDateTime, "ZonedDateTime cannot be null");
    }

    // ==================== FACTORY METHODS - CURRENT TIME ====================

    /**
     * Creates a DateTime representing the current moment in system timezone.
     */
    @Contract(" -> new")
    public static @NotNull DateTime now() {
        return new DateTime(ZonedDateTime.now());
    }

    /**
     * Creates a DateTime representing the current moment in UTC.
     */
    @Contract(" -> new")
    public static @NotNull DateTime nowUTC() {
        return new DateTime(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    /**
     * Creates a DateTime representing the current moment in specified timezone.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime now(@NotNull ZoneId zone) {
        return new DateTime(ZonedDateTime.now(zone));
    }

    /**
     * Creates a DateTime representing the current moment in specified timezone.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime now(@NotNull String zoneId) {
        return now(ZoneId.of(zoneId));
    }

    // ==================== FACTORY METHODS - FROM VALUES ====================

    /**
     * Creates DateTime from year, month, day.
     */
    @Contract("_, _, _ -> new")
    public static @NotNull DateTime of(int year, int month, int day) {
        return of(year, month, day, 0, 0, 0);
    }

    /**
     * Creates DateTime from year, month, day, hour, minute, second.
     */
    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull DateTime of(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        return new DateTime(ZonedDateTime.of(ldt, ZoneId.systemDefault()));
    }

    /**
     * Creates DateTime from LocalDate at start of day.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime of(@NotNull LocalDate date) {
        return new DateTime(ZonedDateTime.of(date, LocalTime.MIN, ZoneId.systemDefault()));
    }

    /**
     * Creates DateTime from LocalDate and LocalTime.
     */
    @Contract("_, _ -> new")
    public static @NotNull DateTime of(@NotNull LocalDate date, @NotNull LocalTime time) {
        return new DateTime(ZonedDateTime.of(date, time, ZoneId.systemDefault()));
    }

    /**
     * Creates DateTime from LocalDateTime.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime of(@NotNull LocalDateTime dateTime) {
        return new DateTime(ZonedDateTime.of(dateTime, ZoneId.systemDefault()));
    }

    /**
     * Creates DateTime from ZonedDateTime.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime of(@NotNull ZonedDateTime zonedDateTime) {
        return new DateTime(zonedDateTime);
    }

    /**
     * Creates DateTime from Instant.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime of(@NotNull Instant instant) {
        return new DateTime(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    /**
     * Creates DateTime from legacy Date.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime of(@NotNull Date date) {
        return of(date.toInstant());
    }

    /**
     * Creates DateTime from epoch milliseconds.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime ofEpochMilli(long epochMilli) {
        return of(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Creates DateTime from epoch seconds.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime ofEpochSecond(long epochSecond) {
        return of(Instant.ofEpochSecond(epochSecond));
    }

    // ==================== FACTORY METHODS - PARSING ====================

    /**
     * Parses DateTime from ISO-8601 string.
     */
    @Contract("_ -> new")
    public static @NotNull DateTime parse(@NotNull String text) {
        try {
            return new DateTime(ZonedDateTime.parse(text));
        } catch (DateTimeParseException e) {
            try {
                return of(LocalDateTime.parse(text));
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Unable to parse: " + text, text, 0);
            }
        }
    }

    /**
     * Parses DateTime using a specific format.
     */
    @Contract("_, _ -> new")
    public static @NotNull DateTime parse(@NotNull String text, @NotNull Format format) {
        return parse(text, format.getFormatter());
    }

    /**
     * Parses DateTime using a custom pattern.
     */
    @Contract("_, _ -> new")
    public static @NotNull DateTime parse(@NotNull String text, @NotNull String pattern) {
        return parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parses DateTime using a DateTimeFormatter.
     */
    @Contract("_, _ -> new")
    public static @NotNull DateTime parse(@NotNull String text, @NotNull DateTimeFormatter formatter) {
        try {
            return new DateTime(ZonedDateTime.parse(text, formatter));
        } catch (DateTimeParseException e) {
            LocalDateTime ldt = LocalDateTime.parse(text, formatter);
            return of(ldt);
        }
    }

    /**
     * Safely parses DateTime, returning Optional.
     */
    public static @NotNull Optional<DateTime> tryParse(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(parse(text));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Safely parses with format.
     */
    public static @NotNull Optional<DateTime> tryParse(@Nullable String text, @NotNull Format format) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(parse(text, format));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Validates if a string can be parsed.
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    /**
     * Validates with format.
     */
    public static boolean isValid(@Nullable String text, @NotNull String pattern) {
        if (text == null || text.isBlank()) return false;
        try {
            parse(text, pattern);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // ==================== SPECIAL DATES ====================

    /**
     * Returns start of today.
     */
    @Contract(" -> new")
    public static @NotNull DateTime today() {
        return of(LocalDate.now());
    }

    /**
     * Returns start of tomorrow.
     */
    @Contract(" -> new")
    public static @NotNull DateTime tomorrow() {
        return today().plusDays(1);
    }

    /**
     * Returns start of yesterday.
     */
    @Contract(" -> new")
    public static @NotNull DateTime yesterday() {
        return today().minusDays(1);
    }

    /**
     * Returns start of current week (Monday).
     */
    @Contract(" -> new")
    public static @NotNull DateTime startOfWeek() {
        return today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Returns end of current week (Sunday).
     */
    @Contract(" -> new")
    public static @NotNull DateTime endOfWeek() {
        return today().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Returns start of current month.
     */
    @Contract(" -> new")
    public static @NotNull DateTime startOfMonth() {
        return today().withDayOfMonth(1);
    }

    /**
     * Returns end of current month.
     */
    @Contract(" -> new")
    public static @NotNull DateTime endOfMonth() {
        return today().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns start of current year.
     */
    @Contract(" -> new")
    public static @NotNull DateTime startOfYear() {
        return today().withDayOfYear(1);
    }

    /**
     * Returns end of current year.
     */
    @Contract(" -> new")
    public static @NotNull DateTime endOfYear() {
        return today().with(TemporalAdjusters.lastDayOfYear());
    }

    // ==================== ADDITION OPERATIONS ====================

    /**
     * Adds the specified amount of time.
     */
    @Contract("_, _ -> new")
    public @NotNull DateTime plus(long amount, @NotNull TemporalUnit unit) {
        return new DateTime(zonedDateTime.plus(amount, unit));
    }

    /**
     * Adds a Duration.
     */
    @Contract("_ -> new")
    public @NotNull DateTime plus(@NotNull Duration duration) {
        return new DateTime(zonedDateTime.plus(duration));
    }

    /**
     * Adds a Period.
     */
    @Contract("_ -> new")
    public @NotNull DateTime plus(@NotNull Period period) {
        return new DateTime(zonedDateTime.plus(period));
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusYears(long years) {
        return plus(years, ChronoUnit.YEARS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusMonths(long months) {
        return plus(months, ChronoUnit.MONTHS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusWeeks(long weeks) {
        return plus(weeks, ChronoUnit.WEEKS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusDays(long days) {
        return plus(days, ChronoUnit.DAYS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusHours(long hours) {
        return plus(hours, ChronoUnit.HOURS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusMinutes(long minutes) {
        return plus(minutes, ChronoUnit.MINUTES);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusSeconds(long seconds) {
        return plus(seconds, ChronoUnit.SECONDS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusMillis(long millis) {
        return plus(millis, ChronoUnit.MILLIS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime plusNanos(long nanos) {
        return plus(nanos, ChronoUnit.NANOS);
    }

    // ==================== SUBTRACTION OPERATIONS ====================

    /**
     * Subtracts the specified amount of time.
     */
    @Contract("_, _ -> new")
    public @NotNull DateTime minus(long amount, @NotNull TemporalUnit unit) {
        return new DateTime(zonedDateTime.minus(amount, unit));
    }

    /**
     * Subtracts a Duration.
     */
    @Contract("_ -> new")
    public @NotNull DateTime minus(@NotNull Duration duration) {
        return new DateTime(zonedDateTime.minus(duration));
    }

    /**
     * Subtracts a Period.
     */
    @Contract("_ -> new")
    public @NotNull DateTime minus(@NotNull Period period) {
        return new DateTime(zonedDateTime.minus(period));
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusYears(long years) {
        return minus(years, ChronoUnit.YEARS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusMonths(long months) {
        return minus(months, ChronoUnit.MONTHS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusWeeks(long weeks) {
        return minus(weeks, ChronoUnit.WEEKS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusDays(long days) {
        return minus(days, ChronoUnit.DAYS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusHours(long hours) {
        return minus(hours, ChronoUnit.HOURS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusMinutes(long minutes) {
        return minus(minutes, ChronoUnit.MINUTES);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusSeconds(long seconds) {
        return minus(seconds, ChronoUnit.SECONDS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusMillis(long millis) {
        return minus(millis, ChronoUnit.MILLIS);
    }

    @Contract("_ -> new")
    public @NotNull DateTime minusNanos(long nanos) {
        return minus(nanos, ChronoUnit.NANOS);
    }

    // ==================== WITH OPERATIONS ====================

    /**
     * Returns a copy with the specified field set.
     */
    @Contract("_ -> new")
    public @NotNull DateTime with(@NotNull TemporalAdjuster adjuster) {
        return new DateTime(zonedDateTime.with(adjuster));
    }

    /**
     * Returns a copy with the specified field set to value.
     */
    @Contract("_, _ -> new")
    public @NotNull DateTime with(@NotNull TemporalField field, long value) {
        return new DateTime(zonedDateTime.with(field, value));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withYear(int year) {
        return new DateTime(zonedDateTime.withYear(year));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withMonth(int month) {
        return new DateTime(zonedDateTime.withMonth(month));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withDayOfMonth(int day) {
        return new DateTime(zonedDateTime.withDayOfMonth(day));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withDayOfYear(int day) {
        return new DateTime(zonedDateTime.withDayOfYear(day));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withHour(int hour) {
        return new DateTime(zonedDateTime.withHour(hour));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withMinute(int minute) {
        return new DateTime(zonedDateTime.withMinute(minute));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withSecond(int second) {
        return new DateTime(zonedDateTime.withSecond(second));
    }

    @Contract("_ -> new")
    public @NotNull DateTime withNano(int nano) {
        return new DateTime(zonedDateTime.withNano(nano));
    }

    /**
     * Returns a copy at the start of the day (00:00:00).
     */
    @Contract(" -> new")
    public @NotNull DateTime atStartOfDay() {
        return new DateTime(zonedDateTime.truncatedTo(ChronoUnit.DAYS));
    }

    /**
     * Returns a copy at the end of the day (23:59:59.999999999).
     */
    @Contract(" -> new")
    public @NotNull DateTime atEndOfDay() {
        return atStartOfDay().plusDays(1).minusNanos(1);
    }

    /**
     * Returns a copy at noon (12:00:00).
     */
    @Contract(" -> new")
    public @NotNull DateTime atNoon() {
        return atStartOfDay().withHour(12);
    }

    /**
     * Returns a copy at midnight (00:00:00).
     */
    @Contract(" -> new")
    public @NotNull DateTime atMidnight() {
        return atStartOfDay();
    }

    // ==================== TIMEZONE OPERATIONS ====================

    /**
     * Converts to a different timezone.
     */
    @Contract("_ -> new")
    public @NotNull DateTime inZone(@NotNull ZoneId zone) {
        return new DateTime(zonedDateTime.withZoneSameInstant(zone));
    }

    /**
     * Converts to a different timezone.
     */
    @Contract("_ -> new")
    public @NotNull DateTime inZone(@NotNull String zoneId) {
        return inZone(ZoneId.of(zoneId));
    }

    /**
     * Converts to UTC.
     */
    @Contract(" -> new")
    public @NotNull DateTime inUTC() {
        return inZone(ZoneId.of("UTC"));
    }

    /**
     * Converts to system default timezone.
     */
    @Contract(" -> new")
    public @NotNull DateTime inSystemZone() {
        return inZone(ZoneId.systemDefault());
    }

    /**
     * Gets the timezone ID.
     */
    public @NotNull ZoneId getZone() {
        return zonedDateTime.getZone();
    }

    /**
     * Gets the timezone offset.
     */
    public @NotNull ZoneOffset getOffset() {
        return zonedDateTime.getOffset();
    }

    // ==================== COMPARISON ====================

    /**
     * Checks if this is before another DateTime.
     */
    public boolean isBefore(@NotNull DateTime other) {
        return zonedDateTime.isBefore(other.zonedDateTime);
    }

    /**
     * Checks if this is after another DateTime.
     */
    public boolean isAfter(@NotNull DateTime other) {
        return zonedDateTime.isAfter(other.zonedDateTime);
    }

    /**
     * Checks if this equals another DateTime (instant-based).
     */
    public boolean isEqual(@NotNull DateTime other) {
        return zonedDateTime.isEqual(other.zonedDateTime);
    }

    /**
     * Checks if this is before or equal to another DateTime.
     */
    public boolean isBeforeOrEqual(@NotNull DateTime other) {
        return isBefore(other) || isEqual(other);
    }

    /**
     * Checks if this is after or equal to another DateTime.
     */
    public boolean isAfterOrEqual(@NotNull DateTime other) {
        return isAfter(other) || isEqual(other);
    }

    /**
     * Checks if this is between two DateTimes (inclusive).
     */
    public boolean isBetween(@NotNull DateTime start, @NotNull DateTime end) {
        return isAfterOrEqual(start) && isBeforeOrEqual(end);
    }

    /**
     * Checks if this is in the past.
     */
    public boolean isPast() {
        return isBefore(now());
    }

    /**
     * Checks if this is in the future.
     */
    public boolean isFuture() {
        return isAfter(now());
    }

    /**
     * Checks if this is today.
     */
    public boolean isToday() {
        return toLocalDate().equals(LocalDate.now());
    }

    /**
     * Checks if this is yesterday.
     */
    public boolean isYesterday() {
        return toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    /**
     * Checks if this is tomorrow.
     */
    public boolean isTomorrow() {
        return toLocalDate().equals(LocalDate.now().plusDays(1));
    }

    /**
     * Checks if this is within the last duration.
     */
    public boolean isWithinLast(@NotNull Duration duration) {
        return isAfter(now().minus(duration));
    }

    /**
     * Checks if this is within the next duration.
     */
    public boolean isWithinNext(@NotNull Duration duration) {
        return isBefore(now().plus(duration));
    }

    @Override
    public int compareTo(@NotNull DateTime other) {
        return zonedDateTime.compareTo(other.zonedDateTime);
    }

    // ==================== DURATION & PERIOD ====================

    /**
     * Calculates duration between this and another DateTime.
     */
    public @NotNull Duration durationUntil(@NotNull DateTime other) {
        return Duration.between(zonedDateTime, other.zonedDateTime);
    }

    /**
     * Calculates duration since another DateTime.
     */
    public @NotNull Duration durationSince(@NotNull DateTime other) {
        return Duration.between(other.zonedDateTime, zonedDateTime);
    }

    /**
     * Calculates amount of time between in specified unit.
     */
    public long until(@NotNull DateTime other, @NotNull TemporalUnit unit) {
        return zonedDateTime.until(other.zonedDateTime, unit);
    }

    /**
     * Gets years until another DateTime.
     */
    public long yearsUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.YEARS);
    }

    /**
     * Gets months until another DateTime.
     */
    public long monthsUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.MONTHS);
    }

    /**
     * Gets days until another DateTime.
     */
    public long daysUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.DAYS);
    }

    /**
     * Gets hours until another DateTime.
     */
    public long hoursUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.HOURS);
    }

    /**
     * Gets minutes until another DateTime.
     */
    public long minutesUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.MINUTES);
    }

    /**
     * Gets seconds until another DateTime.
     */
    public long secondsUntil(@NotNull DateTime other) {
        return until(other, ChronoUnit.SECONDS);
    }

    // ==================== DAY OF WEEK / MONTH QUERIES ====================

    /**
     * Gets the day of week.
     */
    public @NotNull DayOfWeek getDayOfWeek() {
        return zonedDateTime.getDayOfWeek();
    }

    /**
     * Gets the month.
     */
    public @NotNull Month getMonth() {
        return zonedDateTime.getMonth();
    }

    /**
     * Gets day of month (1-31).
     */
    public int getDayOfMonth() {
        return zonedDateTime.getDayOfMonth();
    }

    /**
     * Gets day of year (1-365/366).
     */
    public int getDayOfYear() {
        return zonedDateTime.getDayOfYear();
    }

    /**
     * Gets the year.
     */
    public int getYear() {
        return zonedDateTime.getYear();
    }

    /**
     * Gets month value (1-12).
     */
    public int getMonthValue() {
        return zonedDateTime.getMonthValue();
    }

    /**
     * Gets hour (0-23).
     */
    public int getHour() {
        return zonedDateTime.getHour();
    }

    /**
     * Gets minute (0-59).
     */
    public int getMinute() {
        return zonedDateTime.getMinute();
    }

    /**
     * Gets second (0-59).
     */
    public int getSecond() {
        return zonedDateTime.getSecond();
    }

    /**
     * Gets nanosecond.
     */
    public int getNano() {
        return zonedDateTime.getNano();
    }

    /**
     * Checks if this is a weekend.
     */
    public boolean isWeekend() {
        DayOfWeek day = getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if this is a weekday.
     */
    public boolean isWeekday() {
        return !isWeekend();
    }

    /**
     * Checks if the year is a leap year.
     */
    public boolean isLeapYear() {
        return zonedDateTime.toLocalDate().isLeapYear();
    }

    // ==================== BUSINESS DAY OPERATIONS ====================

    /**
     * Checks if this is a business day (Monday-Friday, excluding holidays).
     * Note: Holiday checking requires custom implementation.
     */
    public boolean isBusinessDay() {
        return isWeekday(); // Override to add holiday logic
    }

    /**
     * Gets the next business day.
     */
    @Contract(" -> new")
    public @NotNull DateTime nextBusinessDay() {
        DateTime next = plusDays(1);
        while (!next.isBusinessDay()) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Gets the previous business day.
     */
    @Contract(" -> new")
    public @NotNull DateTime previousBusinessDay() {
        DateTime prev = minusDays(1);
        while (!prev.isBusinessDay()) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    /**
     * Adds business days.
     */
    @Contract("_ -> new")
    public @NotNull DateTime plusBusinessDays(int days) {
        DateTime result = this;
        int added = 0;
        while (added < days) {
            result = result.plusDays(1);
            if (result.isBusinessDay()) {
                added++;
            }
        }
        return result;
    }

    /**
     * Subtracts business days.
     */
    @Contract("_ -> new")
    public @NotNull DateTime minusBusinessDays(int days) {
        DateTime result = this;
        int subtracted = 0;
        while (subtracted < days) {
            result = result.minusDays(1);
            if (result.isBusinessDay()) {
                subtracted++;
            }
        }
        return result;
    }

    /**
     * Calculates business days between two DateTimes.
     */
    public long businessDaysBetween(@NotNull DateTime other) {
        DateTime start = this.isBefore(other) ? this : other;
        DateTime end = this.isBefore(other) ? other : this;

        long count = 0;
        DateTime current = start;
        while (current.isBefore(end)) {
            if (current.isBusinessDay()) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // ==================== FORMATTING ====================

    /**
     * Formats using predefined format.
     */
    public @NotNull String format(@NotNull Format format) {
        return format.format(zonedDateTime);
    }

    /**
     * Formats using custom pattern.
     */
    public @NotNull String format(@NotNull String pattern) {
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats using DateTimeFormatter.
     */
    public @NotNull String format(@NotNull DateTimeFormatter formatter) {
        return zonedDateTime.format(formatter);
    }

    /**
     * Formats to ISO-8601 string.
     */
    public @NotNull String toISOString() {
        return format(Format.ISO_DATETIME);
    }

    /**
     * Formats to relative time string (e.g., "2 hours ago", "in 3 days").
     */
    public @NotNull String toRelativeString() {
        return toRelativeString(Locale.getDefault());
    }

    /**
     * Formats to relative time string with locale.
     */
    public @NotNull String toRelativeString(@NotNull Locale locale) {
        DateTime now = DateTime.now();
        Duration duration = Duration.between(zonedDateTime, now.zonedDateTime);

        long seconds = Math.abs(duration.getSeconds());
        boolean past = duration.isNegative() || duration.isZero();

        if (seconds < 60) {
            return past ? "just now" : "in a moment";
        }

        if (seconds < 3600) {
            long minutes = seconds / 60;
            return formatRelative(minutes, "minute", past);
        }

        if (seconds < 86400) {
            long hours = seconds / 3600;
            return formatRelative(hours, "hour", past);
        }

        if (seconds < 2592000) {
            long days = seconds / 86400;
            return formatRelative(days, "day", past);
        }

        if (seconds < 31536000) {
            long months = seconds / 2592000;
            return formatRelative(months, "month", past);
        }

        long years = seconds / 31536000;
        return formatRelative(years, "year", past);
    }

    private String formatRelative(long value, String unit, boolean past) {
        String plural = value == 1 ? unit : unit + "s";
        return past ? value + " " + plural + " ago" : "in " + value + " " + plural;
    }

    /**
     * Formats to human-readable string.
     */
    public @NotNull String toHumanString() {
        if (isToday())
            return "Today at " + format("HH:mm");

        if (isYesterday())
            return "Yesterday at " + format("HH:mm");

        if (isTomorrow())
            return "Tomorrow at " + format("HH:mm");

        if (isWithinLast(Duration.ofDays(7)))
            return getDayOfWeek() + " at " + format("HH:mm");

        return format(Format.FRIENDLY_DATETIME);
    }

    // ==================== CONVERSION ====================

    /**
     * Converts to ZonedDateTime.
     */
    public @NotNull ZonedDateTime toZonedDateTime() {
        return zonedDateTime;
    }

    /**
     * Converts to LocalDateTime (loses timezone info).
     */
    public @NotNull LocalDateTime toLocalDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Converts to LocalDate.
     */
    public @NotNull LocalDate toLocalDate() {
        return zonedDateTime.toLocalDate();
    }

    /**
     * Converts to LocalTime.
     */
    public @NotNull LocalTime toLocalTime() {
        return zonedDateTime.toLocalTime();
    }

    /**
     * Converts to Instant.
     */
    public @NotNull Instant toInstant() {
        return zonedDateTime.toInstant();
    }

    /**
     * Converts to legacy Date.
     */
    public @NotNull Date toDate() {
        return Date.from(toInstant());
    }

    /**
     * Converts to epoch milliseconds.
     */
    public long toEpochMilli() {
        return toInstant().toEpochMilli();
    }

    /**
     * Converts to epoch seconds.
     */
    public long toEpochSecond() {
        return toInstant().getEpochSecond();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Returns the minimum of two DateTimes.
     */
    public static @NotNull DateTime min(@NotNull DateTime a, @NotNull DateTime b) {
        return a.isBefore(b) ? a : b;
    }

    /**
     * Returns the maximum of two DateTimes.
     */
    public static @NotNull DateTime max(@NotNull DateTime a, @NotNull DateTime b) {
        return a.isAfter(b) ? a : b;
    }

    /**
     * Creates a copy of this DateTime.
     */
    @Contract(" -> new")
    public @NotNull DateTime copy() {
        return new DateTime(zonedDateTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DateTime other)) return false;
        return zonedDateTime.equals(other.zonedDateTime);
    }

    @Override
    public int hashCode() {
        return zonedDateTime.hashCode();
    }

    @Override
    public String toString() {
        return zonedDateTime.toString();
    }

    // ==================== FORMAT ENUM ====================

    /**
     * Predefined date/time formats.
     */
    public enum Format {
        // Date formats
        ISO_DATE("yyyy-MM-dd"),
        ISO_DATE_TIME("yyyy-MM-dd'T'HH:mm:ss"),
        ISO_DATETIME("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),

        SLASH_DATE("dd/MM/yyyy"),
        SLASH_DATETIME("dd/MM/yyyy HH:mm:ss"),
        SLASH_DATETIME_FULL("dd/MM/yyyy HH:mm:ss.SSS"),

        DASH_DATE("dd-MM-yyyy"),
        DASH_DATETIME("dd-MM-yyyy HH:mm:ss"),

        DOT_DATE("dd.MM.yyyy"),
        DOT_DATETIME("dd.MM.yyyy HH:mm:ss"),

        US_DATE("MM/dd/yyyy"),
        US_DATETIME("MM/dd/yyyy hh:mm:ss a"),

        // Time formats
        TIME_24("HH:mm:ss"),
        TIME_24_SHORT("HH:mm"),
        TIME_12("hh:mm:ss a"),
        TIME_12_SHORT("hh:mm a"),
        TIME_FULL("HH:mm:ss.SSS"),

        // Full formats
        FRIENDLY_DATE("EEEE, MMMM d, yyyy"),
        FRIENDLY_DATETIME("EEEE, MMMM d, yyyy 'at' HH:mm"),
        FRIENDLY_TIME("h:mm a"),

        MONTH_YEAR("MMMM yyyy"),
        MONTH_DAY("MMMM d"),

        // Compact formats
        COMPACT_DATE("yyyyMMdd"),
        COMPACT_DATETIME("yyyyMMddHHmmss"),
        COMPACT_TIMESTAMP("yyyyMMddHHmmssSSS"),

        // RFC formats
        RFC_1123("EEE, dd MMM yyyy HH:mm:ss z"),

        // Custom readable
        LONG_DATE("EEEE, d 'de' MMMM 'de' yyyy"),
        SHORT_DATE("dd/MM/yy"),

        // Timestamp formats
        TIMESTAMP("yyyy-MM-dd HH:mm:ss.SSS"),
        LOG_TIMESTAMP("[yyyy-MM-dd HH:mm:ss.SSS]"),

        // Week formats
        WEEK_DATE("EEEE, MMM d"),
        YEAR_WEEK("yyyy-'W'ww");

        private final DateTimeFormatter formatter;

        Format(String pattern) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        }

        public @NotNull DateTimeFormatter getFormatter() {
            return formatter;
        }

        public @NotNull String format(@NotNull ZonedDateTime dateTime) {
            return dateTime.format(formatter);
        }

        public @NotNull String format(@NotNull LocalDateTime dateTime) {
            return dateTime.format(formatter);
        }

        public @NotNull String format(@NotNull LocalDate date) {
            return date.format(formatter);
        }

        public @NotNull String format(@NotNull LocalTime time) {
            return time.format(formatter);
        }
    }

    // ==================== BUILDER ====================

    /**
     * Builder for constructing DateTime with fluent API.
     */
    public static final class Builder {
        private Integer year;
        private Integer month;
        private Integer day;
        private Integer hour   = 0;
        private Integer minute = 0;
        private Integer second = 0;
        private Integer nano   = 0;
        private ZoneId  zone   = ZoneId.systemDefault();

        private Builder() {}

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder month(int month) {
            this.month = month;
            return this;
        }

        public Builder day(int day) {
            this.day = day;
            return this;
        }

        public Builder hour(int hour) {
            this.hour = hour;
            return this;
        }

        public Builder minute(int minute) {
            this.minute = minute;
            return this;
        }

        public Builder second(int second) {
            this.second = second;
            return this;
        }

        public Builder nano(int nano) {
            this.nano = nano;
            return this;
        }

        public Builder zone(ZoneId zone) {
            this.zone = zone;
            return this;
        }

        public Builder zone(String zoneId) {
            return zone(ZoneId.of(zoneId));
        }

        public Builder utc() {
            return zone(ZoneId.of("UTC"));
        }

        public @NotNull DateTime build() {
            if (year == null || month == null || day == null) {
                throw new IllegalStateException("Year, month, and day must be set");
            }
            LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second, nano);
            return new DateTime(ZonedDateTime.of(ldt, zone));
        }
    }

    /**
     * Creates a builder for fluent DateTime construction.
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    // ==================== RANGE ====================

    /**
     * Represents a range between two DateTimes.
     */
    public static final class Range {
        private final DateTime start;
        private final DateTime end;

        private Range(@NotNull DateTime start, @NotNull DateTime end) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start must be before or equal to end");
            }
            this.start = start;
            this.end   = end;
        }

        public @NotNull DateTime getStart() {
            return start;
        }

        public @NotNull DateTime getEnd() {
            return end;
        }

        /**
         * Checks if the specified DateTime is within the range defined by the start and end DateTimes
         * of this Range instance.
         *
         * @param dateTime the DateTime to check, not null
         * @return true if the specified DateTime is within the range (inclusive), false otherwise
         */
        public boolean contains(@NotNull DateTime dateTime) {
            return dateTime.isBetween(start, end);
        }

        /**
         * Determines whether this range overlaps with another range.
         *
         * @param other the range to check for overlap with, not null
         * @return true if the two ranges overlap, false otherwise
         */
        public boolean overlaps(@NotNull Range other) {
            return start.isBefore(other.end) && other.start.isBefore(end);
        }

        public @NotNull Duration duration() {
            return start.durationUntil(end);
        }

        public long days() {
            return start.daysUntil(end);
        }

        public long hours() {
            return start.hoursUntil(end);
        }

        public long minutes() {
            return start.minutesUntil(end);
        }

        public @NotNull List<DateTime> daysBetween() {
            List<DateTime> days    = new ArrayList<>();
            DateTime       current = start;
            while (current.isBeforeOrEqual(end)) {
                days.add(current);
                current = current.plusDays(1);
            }
            return days;
        }

        @Override
        public String toString() {
            return "[" + start + " to " + end + "]";
        }
    }

    /**
     * Creates a range from this DateTime to another.
     */
    public @NotNull Range rangeTo(@NotNull DateTime end) {
        return new Range(this, end);
    }

    /**
     * Creates a range between two DateTimes.
     */
    public static @NotNull Range between(@NotNull DateTime start, @NotNull DateTime end) {
        return new Range(start, end);
    }

    // ==================== CLOCK UTILITIES ====================

    /**
     * Creates a DateTime from a Clock.
     */
    public static @NotNull DateTime now(@NotNull Clock clock) {
        return new DateTime(ZonedDateTime.now(clock));
    }

    /**
     * Creates a fixed DateTime for testing.
     */
    public static @NotNull DateTime fixed(@NotNull Instant instant, @NotNull ZoneId zone) {
        return new DateTime(ZonedDateTime.ofInstant(instant, zone));
    }

    /**
     * Creates a fixed DateTime for testing (system zone).
     */
    public static @NotNull DateTime fixed(@NotNull Instant instant) {
        return fixed(instant, ZoneId.systemDefault());
    }

    // ==================== COMMON TIMEZONE CONSTANTS ====================

    public static final ZoneId UTC  = ZoneId.of("UTC");
    public static final ZoneId GMT  = ZoneId.of("GMT");
    public static final ZoneId EST  = ZoneId.of("America/New_York");
    public static final ZoneId PST  = ZoneId.of("America/Los_Angeles");
    public static final ZoneId CST  = ZoneId.of("America/Chicago");
    public static final ZoneId JST  = ZoneId.of("Asia/Tokyo");
    public static final ZoneId CET  = ZoneId.of("Europe/Paris");
    public static final ZoneId IST  = ZoneId.of("Asia/Kolkata");
    public static final ZoneId AEST = ZoneId.of("Australia/Sydney");
    public static final ZoneId BRT  = ZoneId.of("America/Sao_Paulo");
}
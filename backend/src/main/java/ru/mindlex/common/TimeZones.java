package ru.mindlex.common;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Безопасное разрешение часового пояса пользователя.
 */
public final class TimeZones {

    public static final ZoneId FALLBACK = ZoneId.of("UTC");

    private TimeZones() {
    }

    public static ZoneId of(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return FALLBACK;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            return FALLBACK;
        }
    }
}

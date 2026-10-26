package ru.mindlex.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Работа со списками, которые хранятся в БД одной строкой через запятую.
 */
public final class TextUtils {

    private TextUtils() {
    }

    public static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public static String joinCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + ", " + right)
                .orElse(null);
    }

    public static String csvOf(String... values) {
        return joinCsv(Arrays.asList(values));
    }
}

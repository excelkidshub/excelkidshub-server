package in.excelkidshub.platform.common.util;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for common validation operations.
 */
public class ValidationUtil {

    private ValidationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Check if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Check if a collection is null or empty.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if an object is null.
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Validate that a string is not null or empty.
     * Throws IllegalArgumentException if validation fails.
     */
    public static void requireNonEmpty(String value, String message) {
        if (isNullOrEmpty(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validate that an object is not null.
     * Throws IllegalArgumentException if validation fails.
     */
    public static void requireNonNull(Object obj, String message) {
        if (isNull(obj)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validate that a collection is not null or empty.
     * Throws IllegalArgumentException if validation fails.
     */
    public static void requireNonEmpty(Collection<?> collection, String message) {
        if (isNullOrEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }
}

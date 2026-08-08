package in.excelkidshub.platform.common.util;

/**
 * Utility class for string operations.
 */
public class StringUtil {

    private StringUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Convert string to lowercase.
     */
    public static String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase();
    }

    /**
     * Convert string to uppercase.
     */
    public static String toUpperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    /**
     * Capitalize the first letter of a string.
     */
    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    /**
     * Truncate a string to a maximum length.
     */
    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * Check if a string contains another string (case-insensitive).
     */
    public static boolean containsIgnoreCase(String source, String search) {
        if (source == null || search == null) {
            return false;
        }
        return source.toLowerCase().contains(search.toLowerCase());
    }

    /**
     * Generate a random alphanumeric string of specified length.
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }
        return result.toString();
    }
}

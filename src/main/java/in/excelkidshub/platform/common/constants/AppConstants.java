package in.excelkidshub.platform.common.constants;

/**
 * Application-wide constants.
 * Contains general application configuration constants.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class - prevent instantiation
    }

    // Application
    public static final String APP_NAME = "ExcelKidsHub Platform";
    public static final String APP_VERSION = "1.0.0";

    // Date/Time
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // Database
    public static final String DEFAULT_SCHEMA = "public";
    public static final String ID_COLUMN = "id";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String CREATED_BY_COLUMN = "created_by";
    public static final String UPDATED_BY_COLUMN = "updated_by";
    public static final String ACTIVE_COLUMN = "active";

    // Entity
    public static final boolean DEFAULT_ACTIVE_STATUS = true;

    // File Upload
    public static final long MAX_FILE_SIZE = 10485760L; // 10MB
    public static final String ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/gif";
    public static final String ALLOWED_DOCUMENT_TYPES = "application/pdf";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // Cache
    public static final long DEFAULT_CACHE_TTL = 3600L; // 1 hour

    // Email
    public static final String EMAIL_FROM_ADDRESS = "noreply@excelkidshub.in";
    public static final String EMAIL_FROM_NAME = "ExcelKidsHub";

    // Status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}

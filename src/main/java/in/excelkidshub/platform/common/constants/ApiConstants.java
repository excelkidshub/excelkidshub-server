package in.excelkidshub.platform.common.constants;

/**
 * API-related constants.
 * Contains endpoint paths, headers, and other API-related constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Utility class - prevent instantiation
    }

    // API Paths
    public static final String API_BASE_PATH = "/api";
    public static final String HEALTH_PATH = "/health";
    public static final String AUTH_PATH = "/auth";
    public static final String PUBLIC_PATH = "/public";

    // Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    // Pagination
    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "size";
    public static final String SORT_PARAM = "sort";
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";

    // CORS
    public static final String ALLOWED_ORIGINS = "AllowedOrigins";
    public static final String ALLOWED_METHODS = "AllowedMethods";
    public static final String ALLOWED_HEADERS = "AllowedHeaders";
}

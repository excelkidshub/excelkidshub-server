package in.excelkidshub.platform.common.constants;

/**
 * Error code constants.
 * Contains standardized error codes for the application.
 */
public final class ErrorCodes {

    private ErrorCodes() {
        // Utility class - prevent instantiation
    }

    // General Errors (1xxx)
    public static final String INTERNAL_SERVER_ERROR = "E1000";
    public static final String BAD_REQUEST = "E1001";
    public static final String UNAUTHORIZED = "E1002";
    public static final String FORBIDDEN = "E1003";
    public static final String NOT_FOUND = "E1004";
    public static final String CONFLICT = "E1005";
    public static final String VALIDATION_ERROR = "E1006";

    // Authentication Errors (2xxx)
    public static final String INVALID_CREDENTIALS = "E2000";
    public static final String TOKEN_EXPIRED = "E2001";
    public static final String TOKEN_INVALID = "E2002";
    public static final String USER_NOT_FOUND = "E2003";
    public static final String USER_ALREADY_EXISTS = "E2004";
    public static final String ACCOUNT_DISABLED = "E2005";
    public static final String ACCOUNT_LOCKED = "E2006";

    // User Errors (3xxx)
    public static final String USER_PROFILE_NOT_FOUND = "E3000";
    public static final String INVALID_EMAIL = "E3001";
    public static final String INVALID_PASSWORD = "E3002";

    // Course Errors (4xxx)
    public static final String COURSE_NOT_FOUND = "E4000";
    public static final String COURSE_ALREADY_EXISTS = "E4001";
    public static final String COURSE_NOT_ACTIVE = "E4002";

    // Subscription Errors (5xxx)
    public static final String SUBSCRIPTION_NOT_FOUND = "E5000";
    public static final String SUBSCRIPTION_EXPIRED = "E5001";
    public static final String SUBSCRIPTION_NOT_ACTIVE = "E5002";
    public static final String PLAN_NOT_FOUND = "E5003";

    // Payment Errors (6xxx)
    public static final String PAYMENT_FAILED = "E6000";
    public static final String PAYMENT_NOT_FOUND = "E6001";
    public static final String INVALID_PAYMENT_AMOUNT = "E6002";

    // Book Errors (7xxx)
    public static final String BOOK_NOT_FOUND = "E7000";
    public static final String BOOK_PAGE_NOT_FOUND = "E7001";
    public static final String BOOK_ALREADY_EXISTS = "E7002";

    // Progress Errors (8xxx)
    public static final String PROGRESS_NOT_FOUND = "E8000";
    public static final String PROGRESS_ALREADY_COMPLETED = "E8001";
}

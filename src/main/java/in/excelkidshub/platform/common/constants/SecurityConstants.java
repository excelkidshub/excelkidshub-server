package in.excelkidshub.platform.common.constants;

/**
 * Security-related constants.
 * Contains security configuration, roles, and authentication constants.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Utility class - prevent instantiation
    }

    // JWT
    public static final String JWT_SECRET_PROPERTY = "app.jwt.secret";
    public static final String JWT_EXPIRATION_PROPERTY = "app.jwt.expiration";
    public static final long DEFAULT_JWT_EXPIRATION = 86400000L; // 24 hours

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_TEACHER = "ROLE_TEACHER";
    public static final String ROLE_PARENT = "ROLE_PARENT";
    public static final String ROLE_STUDENT = "ROLE_STUDENT";

    // Security
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final int PASSWORD_MIN_LENGTH = 8;

    // Permissions
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String PERMISSION_MANAGE = "MANAGE";
}

package in.excelkidshub.platform.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response body for POST /auth/register and POST /auth/login.
 * Contains the JWT token and the authenticated user's profile.
 */
@Data
@Builder
public class AuthResponse {

    private String          token;
    private UserDto         user;
    private SubscriptionDto subscription;  // null when no active subscription
}

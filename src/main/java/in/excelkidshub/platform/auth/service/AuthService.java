package in.excelkidshub.platform.auth.service;

import in.excelkidshub.platform.auth.dto.AuthResponse;
import in.excelkidshub.platform.auth.dto.LoginRequest;
import in.excelkidshub.platform.auth.dto.RegisterRequest;

/**
 * Authentication service contract.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse me(Long userId);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    void verifyEmail(String token);
}

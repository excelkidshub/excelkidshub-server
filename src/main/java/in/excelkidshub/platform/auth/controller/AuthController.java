package in.excelkidshub.platform.auth.controller;

import in.excelkidshub.platform.auth.dto.AuthResponse;
import in.excelkidshub.platform.auth.dto.ForgotPasswordRequest;
import in.excelkidshub.platform.auth.dto.LoginRequest;
import in.excelkidshub.platform.auth.dto.RegisterRequest;
import in.excelkidshub.platform.auth.dto.ResetPasswordRequest;
import in.excelkidshub.platform.auth.dto.UpdateProfileRequest;
import in.excelkidshub.platform.auth.service.AuthService;
import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 *
 * Public:  POST /auth/register, POST /auth/login,
 *          POST /auth/forgot-password, POST /auth/reset-password,
 *          GET  /auth/verify-email
 * JWT:     GET  /auth/me, PUT /auth/me
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Register ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ── Me (current user) ─────────────────────────────────────────────────────

    /**
     * Returns the authenticated user's profile and active subscription.
     * The JWT filter sets the User entity as the principal — cast directly.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal User currentUser) {

        AuthResponse response = authService.me(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile", response));
    }

    // ── Update Profile ─────────────────────────────────────────────────────────

    /**
     * Updates the authenticated user's profile (name, phone).
     * The JWT filter sets the User entity as the principal — cast directly.
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {

        AuthResponse response = authService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());
        // Always return the same message — do not reveal if email exists
        return ResponseEntity.ok(ApiResponse.success(
                "If this email is registered you will receive a reset link shortly."));
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful. You can now log in."));
    }

    // ── Verify Email ──────────────────────────────────────────────────────────

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam("token") String token) {

        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully."));
    }
}

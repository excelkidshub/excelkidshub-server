package in.excelkidshub.platform.auth.service;

import in.excelkidshub.platform.auth.dto.AuthResponse;
import in.excelkidshub.platform.auth.dto.LoginRequest;
import in.excelkidshub.platform.auth.dto.RegisterRequest;
import in.excelkidshub.platform.auth.dto.SubscriptionDto;
import in.excelkidshub.platform.auth.dto.UserDto;
import in.excelkidshub.platform.auth.entity.AuthToken;
import in.excelkidshub.platform.auth.repository.AuthTokenRepository;
import in.excelkidshub.platform.common.exception.BadRequestException;
import in.excelkidshub.platform.common.exception.ConflictException;
import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.common.exception.UnauthorizedException;
import in.excelkidshub.platform.common.security.JwtUtil;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.Role;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.RoleRepository;
import in.excelkidshub.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core authentication business logic.
 *
 * Covers: register, login, /me, forgot-password, reset-password, verify-email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_STUDENT     = "STUDENT";
    private static final String STATUS_ACTIVE    = "ACTIVE";
    private static final String TYPE_PW_RESET    = "PASSWORD_RESET";
    private static final String TYPE_EMAIL_VERIFY = "EMAIL_VERIFY";

    private final UserRepository         userRepository;
    private final RoleRepository         roleRepository;
    private final AuthTokenRepository    authTokenRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtUtil                jwtUtil;
    private final JavaMailSender         mailSender;

    @Value("${app.frontend.main-url:https://excelkidshub.in}")
    private String frontendMainUrl;

    @Value("${app.mail.from:noreply@excelkidshub.in}")
    private String mailFrom;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        Role studentRole = roleRepository.findByName(ROLE_STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role STUDENT not found. Run seed migration."));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(studentRole)
                .emailVerified(false)
                .build();
        user.setActive(true);

        user = userRepository.save(user);
        log.info("New user registered: id={} email={}", user.getId(), email);

        // Send verification email (non-blocking — log error but don't fail registration)
        sendEmailVerification(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), studentRole.getName());
        return buildAuthResponse(token, user, null);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Your account has been disabled. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        log.info("User logged in: id={}", user.getId());

        List<Subscription> activeSubs = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(user.getId(), STATUS_ACTIVE);

        // Pick the most recent subscription if multiple exist
        Subscription activeSub = null;
        if (!activeSubs.isEmpty()) {
            activeSub = activeSubs.stream()
                    .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .orElse(activeSubs.get(0));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().getName());
        return buildAuthResponse(token, user, activeSub);
    }

    // ── /me ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Account is disabled.");
        }

        List<Subscription> activeSubs = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(userId, STATUS_ACTIVE);

        // Pick the most recent subscription if multiple exist
        Subscription activeSub = null;
        if (!activeSubs.isEmpty()) {
            activeSub = activeSubs.stream()
                    .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .orElse(activeSubs.get(0));
        }

        // Re-issue a fresh token so the client always has a non-expiring session
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().getName());
        return buildAuthResponse(token, user, activeSub);
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String normalised = email.toLowerCase().trim();

        // Do not reveal whether the email is registered
        userRepository.findByEmailAndActiveTrue(normalised).ifPresent(user -> {
            authTokenRepository.invalidatePreviousTokens(user.getId(), TYPE_PW_RESET);

            AuthToken resetToken = AuthToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .tokenType(TYPE_PW_RESET)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            authTokenRepository.save(resetToken);

            sendPasswordResetEmail(user, resetToken.getToken());
            log.info("Password reset token issued for user id={}", user.getId());
        });
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset link."));

        if (!TYPE_PW_RESET.equals(authToken.getTokenType())) {
            throw new BadRequestException("Invalid token type.");
        }
        if (!authToken.isValid()) {
            throw new BadRequestException("This password reset link has expired or already been used.");
        }

        User user = authToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        authToken.setUsed(true);
        authTokenRepository.save(authToken);

        log.info("Password reset completed for user id={}", user.getId());
    }

    // ── Verify Email ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void verifyEmail(String token) {
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification link."));

        if (!TYPE_EMAIL_VERIFY.equals(authToken.getTokenType())) {
            throw new BadRequestException("Invalid token type.");
        }
        if (!authToken.isValid()) {
            throw new BadRequestException("This verification link has expired or already been used.");
        }

        User user = authToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        authToken.setUsed(true);
        authTokenRepository.save(authToken);

        log.info("Email verified for user id={}", user.getId());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void sendEmailVerification(User user) {
        try {
            authTokenRepository.invalidatePreviousTokens(user.getId(), TYPE_EMAIL_VERIFY);

            AuthToken verifyToken = AuthToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .tokenType(TYPE_EMAIL_VERIFY)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            authTokenRepository.save(verifyToken);

            String link = frontendMainUrl + "/verify-email.html?token=" + verifyToken.getToken();

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(user.getEmail());
            msg.setSubject("Verify your ExcelKidsHub account");
            msg.setText("Hi " + user.getFirstName() + ",\n\n"
                    + "Please verify your email by clicking the link below:\n\n"
                    + link + "\n\n"
                    + "This link expires in 24 hours.\n\n"
                    + "If you did not create an account, please ignore this email.\n\n"
                    + "— ExcelKidsHub Team");
            mailSender.send(msg);
        } catch (Exception e) {
            // Email failure must never block registration
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            String link = frontendMainUrl + "/reset-password.html?token=" + token;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(user.getEmail());
            msg.setSubject("Reset your ExcelKidsHub password");
            msg.setText("Hi " + user.getFirstName() + ",\n\n"
                    + "We received a request to reset your password. Click the link below:\n\n"
                    + link + "\n\n"
                    + "This link expires in 1 hour.\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "— ExcelKidsHub Team");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private AuthResponse buildAuthResponse(String token, User user, Subscription subscription) {
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getFirstName() + " " + user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : ROLE_STUDENT)
                .emailVerified(user.getEmailVerified())
                .build();

        SubscriptionDto subDto = null;
        if (subscription != null) {
            subDto = SubscriptionDto.builder()
                    .id(subscription.getId())
                    .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                    .status(subscription.getStatus())
                    .startDate(subscription.getStartDate())
                    .endDate(subscription.getEndDate())
                    .active(subscription.getEndDate() != null && !subscription.getEndDate().isBefore(LocalDate.now()))
                    .paymentId(subscription.getPayment() != null ? subscription.getPayment().getId() : null)
                    .build();
        }

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .subscription(subDto)
                .build();
    }
}

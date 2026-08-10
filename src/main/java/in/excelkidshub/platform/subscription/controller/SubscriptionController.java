package in.excelkidshub.platform.subscription.controller;

import in.excelkidshub.platform.auth.dto.SubscriptionDto;
import in.excelkidshub.platform.common.constants.AppConstants;
import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.common.service.EmailService;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Subscription endpoints for the student dashboard.
 */
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    /**
     * Returns the user's current active subscription, or null if none.
     * Dashboard uses this to show plan name, expiry, and renew prompt.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getActiveSubscription(
            @AuthenticationPrincipal User currentUser) {

        List<Subscription> activeSubs = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(currentUser.getId(), "ACTIVE");

        // Filter by valid end date and pick most recent
        SubscriptionDto dto = activeSubs.stream()
                .filter(s -> s.getEndDate() != null && !s.getEndDate().isBefore(LocalDate.now()))
                .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .map(sub -> SubscriptionDto.builder()
                        .id(sub.getId())
                        .planName(sub.getPlan() != null ? sub.getPlan().getName() : null)
                        .status(sub.getStatus())
                        .startDate(sub.getStartDate())
                        .endDate(sub.getEndDate())
                        .active(true)
                        .build()
                )
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.success("Subscription loaded", dto));
    }

    /**
     * Cancel the user's active subscription.
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @AuthenticationPrincipal User currentUser) {

        List<Subscription> activeSubs = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(currentUser.getId(), "ACTIVE");

        if (activeSubs.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No active subscription found to cancel", null));
        }

        // Cancel all active subscriptions (should typically be only one)
        for (Subscription sub : activeSubs) {
            sub.setStatus(AppConstants.STATUS_CANCELLED);
            sub.setActive(false);
            subscriptionRepository.save(sub);

            // Send cancellation notification
            String userName = currentUser.getFirstName() != null ? currentUser.getFirstName() : "User";
            if (currentUser.getLastName() != null) {
                userName += " " + currentUser.getLastName();
            }
            String planName = sub.getPlan() != null ? sub.getPlan().getName() : "Your subscription";
            emailService.sendSubscriptionCancellationNotification(currentUser.getEmail(), userName, planName);
        }

        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }
}

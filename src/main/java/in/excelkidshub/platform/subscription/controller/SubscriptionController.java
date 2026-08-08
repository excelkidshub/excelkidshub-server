package in.excelkidshub.platform.subscription.controller;

import in.excelkidshub.platform.auth.dto.SubscriptionDto;
import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Subscription endpoints for the student dashboard.
 */
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Returns the user's current active subscription, or null if none.
     * Dashboard uses this to show plan name, expiry, and renew prompt.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getActiveSubscription(
            @AuthenticationPrincipal User currentUser) {

        Optional<Subscription> subOpt = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(currentUser.getId(), "ACTIVE")
                .filter(s -> s.getEndDate() != null && !s.getEndDate().isBefore(LocalDate.now()));

        SubscriptionDto dto = subOpt.map(sub -> SubscriptionDto.builder()
                .id(sub.getId())
                .planName(sub.getPlan() != null ? sub.getPlan().getName() : null)
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .active(true)
                .build()
        ).orElse(null);

        return ResponseEntity.ok(ApiResponse.success("Subscription loaded", dto));
    }
}

package in.excelkidshub.platform.payment.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.payment.dto.RefundRequest;
import in.excelkidshub.platform.payment.dto.RefundResponse;
import in.excelkidshub.platform.payment.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user-facing refund operations.
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /**
     * Request a refund for a payment.
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        RefundResponse response = refundService.requestRefund(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Refund request submitted", response));
    }

    /**
     * Get refund status for a subscription.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundStatus(
            @RequestParam Long subscriptionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        RefundResponse response = refundService.getRefundStatusBySubscription(subscriptionId, userId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success("No refund request found for this subscription", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Refund status loaded", response));
    }

    /**
     * Check if a subscription is eligible for refund.
     */
    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<Boolean>> checkEligibility(
            @RequestParam Long subscriptionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        boolean eligible = refundService.isEligibleForRefundBySubscription(subscriptionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Eligibility checked", eligible));
    }
}

package in.excelkidshub.platform.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /payments/apply-coupon.
 * Used when a 100% discount coupon makes the final amount ₹0 — no Razorpay needed.
 */
@Data
public class ApplyCouponRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotBlank(message = "Coupon code is required")
    private String couponCode;
}

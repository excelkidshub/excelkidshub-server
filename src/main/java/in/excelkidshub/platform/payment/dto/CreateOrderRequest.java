package in.excelkidshub.platform.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /payments/create-order.
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    /** Optional coupon code to apply discount. */
    private String couponCode;
}

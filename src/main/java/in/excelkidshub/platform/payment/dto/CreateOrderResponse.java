package in.excelkidshub.platform.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response from POST /payments/create-order.
 * Frontend uses orderId + razorpayKeyId to open Razorpay Checkout modal.
 */
@Data
@Builder
public class CreateOrderResponse {

    private String     orderId;          // Razorpay order ID  (order_xxx)
    private BigDecimal amount;           // Final amount in INR (after coupon)
    private BigDecimal originalAmount;   // Original plan price
    private String     currency;         // "INR"
    private String     razorpayKeyId;    // Public key for Razorpay Checkout JS
    private Long       planId;
    private String     planName;
    private String     couponCode;       // echoed back if applied
    private BigDecimal discountAmount;   // how much was discounted (0 if no coupon)
}

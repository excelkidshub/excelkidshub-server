package in.excelkidshub.platform.payment.service;

import in.excelkidshub.platform.payment.dto.ApplyCouponRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderResponse;
import in.excelkidshub.platform.payment.dto.SubscriptionActivatedResponse;
import in.excelkidshub.platform.payment.dto.VerifyPaymentRequest;

/**
 * Payment service contract — Razorpay integration.
 */
public interface PaymentService {

    CreateOrderResponse createOrder(Long userId, CreateOrderRequest request);

    SubscriptionActivatedResponse verifyPayment(Long userId, VerifyPaymentRequest request);

    /**
     * Activate a subscription using a 100% discount coupon — no Razorpay payment.
     * Only valid when the coupon reduces the final amount to exactly ₹0.
     */
    SubscriptionActivatedResponse applyCoupon(Long userId, ApplyCouponRequest request);

    void handleWebhook(String rawBody, String razorpaySignature);
}

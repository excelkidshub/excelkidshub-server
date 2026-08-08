package in.excelkidshub.platform.payment.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.payment.dto.ApplyCouponRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderResponse;
import in.excelkidshub.platform.payment.dto.SubscriptionActivatedResponse;
import in.excelkidshub.platform.payment.dto.VerifyPaymentRequest;
import in.excelkidshub.platform.payment.service.PaymentService;
import in.excelkidshub.platform.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment endpoints.
 * POST /payments/create-order  — create Razorpay order, return key + orderId to frontend
 * POST /payments/verify        — verify signature, activate subscription
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User currentUser) {

        CreateOrderResponse response = paymentService.createOrder(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Order created", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<SubscriptionActivatedResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal User currentUser) {

        SubscriptionActivatedResponse response = paymentService.verifyPayment(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified. Subscription activated.", response));
    }

    /**
     * Apply a 100% discount coupon — activates subscription without Razorpay.
     * Called by the frontend when finalAmount === 0 after coupon validation.
     */
    @PostMapping("/apply-coupon")
    public ResponseEntity<ApiResponse<SubscriptionActivatedResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            @AuthenticationPrincipal User currentUser) {

        SubscriptionActivatedResponse response = paymentService.applyCoupon(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Coupon applied. Subscription activated.", response));
    }
}

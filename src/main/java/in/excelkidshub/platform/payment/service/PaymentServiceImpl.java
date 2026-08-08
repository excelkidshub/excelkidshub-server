package in.excelkidshub.platform.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.excelkidshub.platform.common.config.AppConfig;
import in.excelkidshub.platform.common.exception.BadRequestException;
import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.payment.dto.ApplyCouponRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderRequest;
import in.excelkidshub.platform.payment.dto.CreateOrderResponse;
import in.excelkidshub.platform.payment.dto.SubscriptionActivatedResponse;
import in.excelkidshub.platform.payment.dto.VerifyPaymentRequest;
import in.excelkidshub.platform.payment.entity.Payment;
import in.excelkidshub.platform.payment.repository.PaymentRepository;
import in.excelkidshub.platform.subscription.entity.Coupon;
import in.excelkidshub.platform.subscription.entity.Plan;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.CouponRepository;
import in.excelkidshub.platform.subscription.repository.PlanRepository;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Razorpay payment integration.
 *
 * Flow:
 *  1. createOrder  → create Razorpay order, save PENDING Payment record
 *  2. verifyPayment → verify HMAC signature, activate subscription, update Payment to SUCCESS
 *  3. handleWebhook → backup verification from Razorpay server-to-server event
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED  = "FAILED";
    private static final String SUB_ACTIVE     = "ACTIVE";

    private final AppConfig              appConfig;
    private final PlanRepository         planRepository;
    private final UserRepository         userRepository;
    private final PaymentRepository      paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CouponRepository       couponRepository;

    // ── Create Razorpay Order ─────────────────────────────────────────────────

    @Override
    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        BigDecimal originalAmount = plan.getPrice();
        BigDecimal finalAmount    = originalAmount;
        String     couponCode     = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Apply coupon if provided
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Optional<Coupon> couponOpt = couponRepository
                    .findByCodeIgnoreCase(request.getCouponCode().trim());

            if (couponOpt.isPresent() && couponOpt.get().isValid()) {
                Coupon coupon = couponOpt.get();
                finalAmount   = coupon.applyTo(originalAmount);
                discountAmount = originalAmount.subtract(finalAmount);
                couponCode     = coupon.getCode();
                log.info("Coupon {} applied: discount={}", couponCode, discountAmount);
            }
        }

        // Razorpay expects amount in paise (multiply by 100)
        long amountInPaise = finalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        // Guard: Razorpay minimum order is 100 paise (₹1)
        // A 100% coupon should use /payments/apply-coupon instead
        if (amountInPaise < 100) {
            throw new BadRequestException(
                "Amount is too low for payment processing (minimum ₹1). " +
                "If you have a 100% discount coupon, use the coupon activation flow.");
        }

        try {
            RazorpayClient razorpay = getRazorpayClient();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt",  "order_" + userId + "_" + System.currentTimeMillis());

            log.info("Creating Razorpay order: planId={} amountPaise={} keyId={}",
                    plan.getId(), amountInPaise,
                    appConfig.getRazorpay().getKeyId() != null
                        ? appConfig.getRazorpay().getKeyId().substring(0, Math.min(12, appConfig.getRazorpay().getKeyId().length())) + "..."
                        : "NULL");

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // Save PENDING payment record
            Payment payment = Payment.builder()
                    .user(user)
                    .amount(finalAmount)
                    .currency("INR")
                    .status(STATUS_PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .build();
            paymentRepository.save(payment);

            log.info("Razorpay order created: {} for user {} plan {}", razorpayOrderId, userId, plan.getName());

            return CreateOrderResponse.builder()
                    .orderId(razorpayOrderId)
                    .amount(finalAmount)
                    .originalAmount(originalAmount)
                    .currency("INR")
                    .razorpayKeyId(appConfig.getRazorpay().getKeyId())
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .couponCode(couponCode)
                    .discountAmount(discountAmount)
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for userId={} planId={}: {}",
                    userId, plan.getId(), e.getMessage());
            // Surface the real Razorpay error message so it appears in the frontend alert
            String razorpayMsg = e.getMessage() != null ? e.getMessage() : "Unknown Razorpay error";
            throw new BadRequestException("Payment gateway error: " + razorpayMsg);
        }
    }

    // ── Verify Payment + Activate Subscription ────────────────────────────────

    @Override
    @Transactional
    public SubscriptionActivatedResponse verifyPayment(Long userId, VerifyPaymentRequest request) {
        // 1. Verify HMAC-SHA256 signature
        verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        // 2. Prevent duplicate activation
        if (paymentRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            // Already processed — find and return existing subscription
            Payment existing = paymentRepository
                    .findByRazorpayPaymentId(request.getRazorpayPaymentId())
                    .orElseThrow();
            if (existing.getSubscription() != null) {
                Subscription sub = existing.getSubscription();
                return toActivatedResponse(sub);
            }
        }

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 3. Find the PENDING payment by order ID and update it
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseGet(() -> Payment.builder().user(user).currency("INR")
                        .amount(plan.getPrice()).build());

        payment.setStatus(STATUS_SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentDate(LocalDateTime.now());

        // 4. Create new Subscription record
        LocalDate today = LocalDate.now();
        int durationMonths = plan.getDurationMonths() > 0 ? plan.getDurationMonths() : 1;

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(today)
                .endDate(today.plusMonths(durationMonths))
                .status(SUB_ACTIVE)
                .build();
        subscription.setActive(true);

        subscription = subscriptionRepository.save(subscription);
        payment.setSubscription(subscription);
        paymentRepository.save(payment);

        log.info("Subscription activated: userId={} planId={} endDate={}", userId, plan.getId(), subscription.getEndDate());

        return toActivatedResponse(subscription);
    }

    // ── Apply 100% Coupon (no Razorpay needed) ────────────────────────────────

    @Override
    @Transactional
    public SubscriptionActivatedResponse applyCoupon(Long userId, ApplyCouponRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate coupon
        Coupon coupon = couponRepository
                .findByCodeIgnoreCase(request.getCouponCode().trim())
                .orElseThrow(() -> new BadRequestException("Invalid coupon code."));

        if (!coupon.isValid()) {
            throw new BadRequestException("This coupon has expired or reached its usage limit.");
        }

        // Verify the coupon actually makes the price zero
        java.math.BigDecimal finalAmount = coupon.applyTo(plan.getPrice());
        if (finalAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new BadRequestException(
                "This coupon does not reduce the price to zero. "
                + "Please use the normal payment flow for partial discounts.");
        }

        // Create subscription directly — no payment required
        LocalDate today = LocalDate.now();
        int durationMonths = plan.getDurationMonths() > 0 ? plan.getDurationMonths() : 1;

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(today)
                .endDate(today.plusMonths(durationMonths))
                .status(SUB_ACTIVE)
                .build();
        subscription.setActive(true);

        subscription = subscriptionRepository.save(subscription);

        // Record a zero-value payment for audit trail
        Payment payment = Payment.builder()
                .user(user)
                .subscription(subscription)
                .amount(java.math.BigDecimal.ZERO)
                .currency("INR")
                .status(STATUS_SUCCESS)
                .paymentDate(LocalDateTime.now())
                .razorpayOrderId("COUPON-" + coupon.getCode() + "-" + userId)
                .build();
        paymentRepository.save(payment);

        // Increment coupon usage count
        couponRepository.incrementUsedCount(coupon.getId());

        log.info("Free subscription via coupon {}: userId={} planId={} endDate={}",
                coupon.getCode(), userId, plan.getId(), subscription.getEndDate());

        return toActivatedResponse(subscription);
    }

    // ── Webhook Handler ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handleWebhook(String rawBody, String razorpaySignature) {
        // Verify webhook signature
        try {
            boolean valid = Utils.verifyWebhookSignature(
                    rawBody,
                    razorpaySignature,
                    appConfig.getRazorpay().getWebhookSecret()
            );
            if (!valid) {
                log.warn("Invalid Razorpay webhook signature — ignoring");
                return;
            }
        } catch (RazorpayException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return;
        }

        JSONObject payload = new JSONObject(rawBody);
        String event = payload.optString("event");
        log.info("Razorpay webhook event: {}", event);

        if ("payment.captured".equals(event)) {
            JSONObject paymentEntity = payload
                    .optJSONObject("payload")
                    .optJSONObject("payment")
                    .optJSONObject("entity");

            if (paymentEntity != null) {
                String razorpayPaymentId = paymentEntity.optString("id");
                String razorpayOrderId   = paymentEntity.optString("order_id");

                // Only process if not already handled by verify endpoint
                if (!paymentRepository.existsByRazorpayPaymentId(razorpayPaymentId)) {
                    paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                        payment.setStatus(STATUS_SUCCESS);
                        payment.setRazorpayPaymentId(razorpayPaymentId);
                        payment.setPaymentDate(LocalDateTime.now());
                        paymentRepository.save(payment);
                        log.info("Webhook: payment {} marked SUCCESS via webhook", razorpayPaymentId);
                    });
                }
            }
        } else if ("payment.failed".equals(event)) {
            JSONObject paymentEntity = payload
                    .optJSONObject("payload")
                    .optJSONObject("payment")
                    .optJSONObject("entity");

            if (paymentEntity != null) {
                String razorpayOrderId = paymentEntity.optString("order_id");
                paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                    payment.setStatus(STATUS_FAILED);
                    payment.setFailureReason(paymentEntity.optString("error_description", "Payment failed"));
                    paymentRepository.save(payment);
                    log.info("Webhook: order {} marked FAILED", razorpayOrderId);
                });
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            boolean valid = Utils.verifyPaymentSignature(
                    new JSONObject().put("razorpay_order_id", orderId)
                            .put("razorpay_payment_id", paymentId)
                            .put("razorpay_signature", signature),
                    appConfig.getRazorpay().getKeySecret()
            );
            if (!valid) {
                log.warn("Payment signature verification failed for order {}", orderId);
                throw new BadRequestException("Payment verification failed. Please contact support.");
            }
        } catch (RazorpayException e) {
            log.error("Razorpay signature verification error: {}", e.getMessage());
            throw new BadRequestException("Payment verification failed. Please contact support.");
        }
    }

    private RazorpayClient getRazorpayClient() throws RazorpayException {
        return new RazorpayClient(
                appConfig.getRazorpay().getKeyId(),
                appConfig.getRazorpay().getKeySecret()
        );
    }

    private SubscriptionActivatedResponse toActivatedResponse(Subscription sub) {
        return SubscriptionActivatedResponse.builder()
                .subscriptionId(sub.getId())
                .planName(sub.getPlan() != null ? sub.getPlan().getName() : null)
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .build();
    }
}

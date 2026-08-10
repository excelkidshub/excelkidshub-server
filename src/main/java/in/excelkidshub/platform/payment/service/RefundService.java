package in.excelkidshub.platform.payment.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.excelkidshub.platform.common.config.AppConfig;
import in.excelkidshub.platform.common.exception.BadRequestException;
import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.payment.dto.AdminRefundActionRequest;
import in.excelkidshub.platform.payment.dto.RefundRequest;
import in.excelkidshub.platform.payment.dto.RefundResponse;
import in.excelkidshub.platform.payment.entity.Payment;
import in.excelkidshub.platform.payment.entity.Refund;
import in.excelkidshub.platform.payment.repository.PaymentRepository;
import in.excelkidshub.platform.payment.repository.RefundRepository;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import in.excelkidshub.platform.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/**
 * Service for refund operations.
 * Handles refund eligibility, request processing, and admin actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final AppConfig appConfig;
    private final EmailService emailService;

    // 7-day refund window in calendar days
    private static final int REFUND_WINDOW_DAYS = 7;
    // Use India timezone for consistency
    private static final ZoneId TIMEZONE = ZoneId.of("Asia/Kolkata");

    /**
     * Check if a payment is eligible for refund.
     * Eligibility: payment succeeded, within 7 calendar days, no existing refund.
     */
    public boolean isEligibleForRefund(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        // Verify user owns the payment
        if (!payment.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to check refund eligibility for payment {} owned by user {}",
                    userId, paymentId, payment.getUser().getId());
            return false;
        }

        // Payment must be successful
        if (!PaymentServiceImpl.STATUS_SUCCESS.equals(payment.getStatus())) {
            log.info("Payment {} is not successful (status: {})", paymentId, payment.getStatus());
            return false;
        }

        // Check if refund already exists
        if (refundRepository.existsByPaymentId(paymentId)) {
            log.info("Refund already exists for payment {}", paymentId);
            return false;
        }

        // Check 7-day window
        LocalDateTime paymentDate = payment.getPaymentDate();
        if (paymentDate == null) {
            log.warn("Payment {} has no payment date", paymentId);
            return false;
        }

        LocalDateTime deadline = paymentDate.plusDays(REFUND_WINDOW_DAYS);
        LocalDateTime now = LocalDateTime.now(TIMEZONE);

        boolean withinWindow = now.isBefore(deadline) || now.isEqual(deadline);
        if (!withinWindow) {
            log.info("Payment {} is outside 7-day refund window (paid: {}, deadline: {}, now: {})",
                    paymentId, paymentDate, deadline, now);
        }

        return withinWindow;
    }

    /**
     * Request a refund for a subscription.
     */
    @Transactional
    public RefundResponse requestRefund(Long userId, RefundRequest request) {
        Long subscriptionId = request.getSubscriptionId();

        // Get subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));

        // Verify ownership
        if (!subscription.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only request refunds for your own subscriptions.");
        }

        // Get payment from subscription
        Payment payment = subscription.getPayment();
        if (payment == null) {
            throw new BadRequestException("No payment found for this subscription.");
        }

        // Verify eligibility
        if (!isEligibleForRefund(payment.getId(), userId)) {
            throw new BadRequestException("This subscription is not eligible for refund. " +
                    "It may be outside the 7-day refund period, already refunded, or not successful.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Create refund request
        Refund refund = Refund.builder()
                .payment(payment)
                .user(user)
                .subscription(subscription)
                .requestedAt(LocalDateTime.now(TIMEZONE))
                .requestReason(request.getReason())
                .status(Refund.STATUS_REQUESTED)
                .refundAmount(payment.getAmount()) // Full refund for MVP
                .build();

        refund = refundRepository.save(refund);

        log.info("Refund request created: id={}, subscriptionId={}, userId={}, amount={}",
                refund.getId(), subscriptionId, userId, payment.getAmount());

        // Notify admin about new refund request
        String userName = user.getFirstName() != null ? user.getFirstName() : "User";
        if (user.getLastName() != null) {
            userName += " " + user.getLastName();
        }
        emailService.sendRefundRequestNotification(user.getEmail(), userName, payment.getId(), request.getReason());

        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(payment.getId())
                .status(refund.getStatus())
                .message("Your refund request has been submitted. Our team will review it and contact you.")
                .requestedAt(refund.getRequestedAt())
                .refundAmount(refund.getRefundAmount())
                .build();
    }

    /**
     * Get refund status for a payment.
     */
    public RefundResponse getRefundStatus(Long paymentId, Long userId) {
        Refund refund = refundRepository.findByPaymentIdAndUserId(paymentId, userId);
        if (refund == null) {
            return null;
        }
        return toRefundResponse(refund);
    }

    /**
     * Get refund status for a subscription.
     */
    public RefundResponse getRefundStatusBySubscription(Long subscriptionId, Long userId) {
        Refund refund = refundRepository.findBySubscriptionIdAndUserId(subscriptionId, userId);
        if (refund == null) {
            return null;
        }
        return toRefundResponse(refund);
    }

    /**
     * Check if a subscription is eligible for refund.
     */
    public boolean isEligibleForRefundBySubscription(Long subscriptionId, Long userId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null || !subscription.getUser().getId().equals(userId)) {
            return false;
        }

        Payment payment = subscription.getPayment();
        if (payment == null) {
            return false;
        }

        return isEligibleForRefund(payment.getId(), userId);
    }

    /**
     * Admin: Get all refund requests with optional status filter.
     */
    public List<Refund> getRefundRequests(String status) {
        if (status == null || status.isEmpty()) {
            return refundRepository.findAll();
        }
        return refundRepository.findByStatus(status);
    }

    /**
     * Admin: Get pending refund requests (REQUESTED or APPROVED).
     */
    public List<Refund> getPendingRefunds() {
        return refundRepository.findPendingRefunds();
    }

    /**
     * Admin: Process refund action (approve/reject/mark refunded).
     */
    @Transactional
    public RefundResponse processRefundAction(Long refundId, Long adminUserId, AdminRefundActionRequest request) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", refundId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminUserId));

        String action = request.getAction();
        LocalDateTime now = LocalDateTime.now(TIMEZONE);

        switch (action) {
            case "APPROVE":
                if (!Refund.STATUS_REQUESTED.equals(refund.getStatus())) {
                    throw new BadRequestException("Can only approve refunds in REQUESTED status.");
                }
                refund.setStatus(Refund.STATUS_APPROVED);
                refund.setReviewedAt(now);
                refund.setReviewedBy(admin);
                refund.setAdminNote(request.getAdminNote());
                log.info("Refund {} approved by admin {}", refundId, adminUserId);

                // Notify customer about approval
                String userName = refund.getUser().getFirstName() != null ? refund.getUser().getFirstName() : "User";
                if (refund.getUser().getLastName() != null) {
                    userName += " " + refund.getUser().getLastName();
                }
                String approvedAmountStr = refund.getRefundAmount() != null ? "₹" + refund.getRefundAmount() : "the full amount";
                emailService.sendRefundApprovedNotification(refund.getUser().getEmail(), userName, approvedAmountStr);
                break;

            case "REJECT":
                if (!Refund.STATUS_REQUESTED.equals(refund.getStatus())) {
                    throw new BadRequestException("Can only reject refunds in REQUESTED status.");
                }
                refund.setStatus(Refund.STATUS_REJECTED);
                refund.setReviewedAt(now);
                refund.setReviewedBy(admin);
                refund.setAdminNote(request.getAdminNote());
                log.info("Refund {} rejected by admin {}", refundId, adminUserId);

                // Notify customer about rejection
                String rejectUserName = refund.getUser().getFirstName() != null ? refund.getUser().getFirstName() : "User";
                if (refund.getUser().getLastName() != null) {
                    rejectUserName += " " + refund.getUser().getLastName();
                }
                emailService.sendRefundRejectedNotification(refund.getUser().getEmail(), rejectUserName, request.getAdminNote());
                break;

            case "MARK_REFUNDED":
                if (!Refund.STATUS_APPROVED.equals(refund.getStatus())) {
                    throw new BadRequestException("Can only mark as refunded refunds in APPROVED status.");
                }
                
                // If razorpayRefundId is provided, use manual mode
                if (request.getRazorpayRefundId() != null && !request.getRazorpayRefundId().isEmpty()) {
                    refund.setStatus(Refund.STATUS_REFUNDED);
                    refund.setProcessedAt(now);
                    refund.setProcessedBy(admin);
                    refund.setRazorpayRefundId(request.getRazorpayRefundId());
                    refund.setAdminNote(request.getAdminNote());

                    cancelSubscription(refund);

                    log.info("Refund {} marked as refunded manually by admin {}, Razorpay refund ID: {}",
                            refundId, adminUserId, request.getRazorpayRefundId());
                } else {
                    // Process refund automatically via Razorpay API
                    try {
                        String razorpayRefundId = processRazorpayRefund(refund.getPayment(), refund.getRefundAmount());
                        refund.setStatus(Refund.STATUS_REFUNDED);
                        refund.setProcessedAt(now);
                        refund.setProcessedBy(admin);
                        refund.setRazorpayRefundId(razorpayRefundId);
                        refund.setAdminNote(request.getAdminNote());

                        cancelSubscription(refund);

                        log.info("Refund {} processed automatically via Razorpay API by admin {}, Razorpay refund ID: {}",
                                refundId, adminUserId, razorpayRefundId);
                    } catch (RazorpayException e) {
                        log.error("Razorpay refund failed for refund {}: {}", refundId, e.getMessage());
                        throw new BadRequestException("Failed to process Razorpay refund: " + e.getMessage());
                    }
                }

                // Notify customer about refund processed
                String processedUserName = refund.getUser().getFirstName() != null ? refund.getUser().getFirstName() : "User";
                if (refund.getUser().getLastName() != null) {
                    processedUserName += " " + refund.getUser().getLastName();
                }
                String refundAmountStr = refund.getRefundAmount() != null ? "₹" + refund.getRefundAmount() : "the full amount";
                emailService.sendRefundProcessedNotification(refund.getUser().getEmail(), processedUserName, refundAmountStr);
                break;

            default:
                throw new BadRequestException("Invalid action: " + action + ". Valid actions: APPROVE, REJECT, MARK_REFUNDED");
        }

        refund = refundRepository.save(refund);
        return toRefundResponse(refund);
    }

    /**
     * Process Razorpay refund via API.
     * Calls Razorpay Refund API to process the refund automatically.
     */
    private String processRazorpayRefund(Payment payment, BigDecimal refundAmount) throws RazorpayException {
        RazorpayClient razorpay = getRazorpayClient();
        
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", refundAmount.multiply(BigDecimal.valueOf(100)).longValue()); // Amount in paise
        refundRequest.put("payment_id", payment.getRazorpayPaymentId());
        
        log.info("Processing Razorpay refund for paymentId={}, amountPaise={}", 
                payment.getRazorpayPaymentId(), refundRequest.get("amount"));
        
        com.razorpay.Refund razorpayRefund = razorpay.payments.refund(refundRequest);
        String razorpayRefundId = razorpayRefund.get("id");
        
        log.info("Razorpay refund processed successfully: refundId={}", razorpayRefundId);
        return razorpayRefundId;
    }

    /**
     * Get Razorpay client instance.
     */
    private RazorpayClient getRazorpayClient() throws RazorpayException {
        String keyId = appConfig.getRazorpay().getKeyId();
        String keySecret = appConfig.getRazorpay().getKeySecret();
        
        if (keyId == null || keySecret == null) {
            throw new BadRequestException("Razorpay credentials not configured");
        }
        
        return new RazorpayClient(keyId, keySecret);
    }

    /**
     * Cancel subscription when refund is completed.
     */
    private void cancelSubscription(Refund refund) {
        Subscription subscription = refund.getSubscription();
        if (subscription != null) {
            subscription.setStatus(in.excelkidshub.platform.common.constants.AppConstants.STATUS_CANCELLED);
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
            log.info("Subscription {} cancelled due to refund {}", subscription.getId(), refund.getId());
        }
    }

    /**
     * Convert Refund entity to RefundResponse DTO.
     */
    private RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment() != null ? refund.getPayment().getId() : null)
                .status(refund.getStatus())
                .message(getStatusMessage(refund.getStatus()))
                .requestedAt(refund.getRequestedAt())
                .reviewedAt(refund.getReviewedAt())
                .processedAt(refund.getProcessedAt())
                .refundAmount(refund.getRefundAmount())
                .build();
    }

    /**
     * Get user-friendly message based on refund status.
     */
    private String getStatusMessage(String status) {
        switch (status) {
            case Refund.STATUS_REQUESTED:
                return "Your refund request is under review.";
            case Refund.STATUS_APPROVED:
                return "Your refund request has been approved. The refund will be processed to your original payment method.";
            case Refund.STATUS_REJECTED:
                return "Your refund request was not approved. Please contact ExcelKidsHub Support if you have questions.";
            case Refund.STATUS_REFUNDED:
                return "Your refund has been processed. Your digital subscription access has ended.";
            default:
                return "Unknown status";
        }
    }
}

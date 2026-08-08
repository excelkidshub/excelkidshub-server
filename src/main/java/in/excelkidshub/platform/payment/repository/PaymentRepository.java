package in.excelkidshub.platform.payment.repository;

import in.excelkidshub.platform.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by user ID.
     */
    List<Payment> findByUserId(Long userId);

    /**
     * Find payments by subscription ID.
     */
    List<Payment> findBySubscriptionId(Long subscriptionId);

    /**
     * Find payment by Razorpay payment ID.
     */
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Find payment by Razorpay order ID.
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Find payments by status.
     */
    List<Payment> findByStatus(String status);

    /**
     * Find payments by user ID and status.
     */
    List<Payment> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find payments within date range.
     */
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if payment exists by Razorpay payment ID.
     */
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}

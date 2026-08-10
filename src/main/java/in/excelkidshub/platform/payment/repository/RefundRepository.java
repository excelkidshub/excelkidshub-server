package in.excelkidshub.platform.payment.repository;

import in.excelkidshub.platform.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Refund entity.
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * Find refunds by user ID.
     */
    List<Refund> findByUserId(Long userId);

    /**
     * Find refunds by payment ID.
     */
    List<Refund> findByPaymentId(Long paymentId);

    /**
     * Find refund by payment ID (single, most recent).
     */
    Optional<Refund> findFirstByPaymentIdOrderByRequestedAtDesc(Long paymentId);

    /**
     * Find refund by payment ID and user ID.
     */
    Refund findByPaymentIdAndUserId(Long paymentId, Long userId);

    /**
     * Find refund by subscription ID and user ID.
     */
    Refund findBySubscriptionIdAndUserId(Long subscriptionId, Long userId);

    /**
     * Find refunds by status.
     */
    List<Refund> findByStatus(String status);

    /**
     * Find refunds by user ID and status.
     */
    List<Refund> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find refunds requested within date range.
     */
    List<Refund> findByRequestedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if a refund exists for a payment with specific statuses.
     */
    @Query("SELECT COUNT(r) > 0 FROM Refund r WHERE r.payment.id = :paymentId AND r.status IN :statuses")
    boolean existsByPaymentIdAndStatusIn(@Param("paymentId") Long paymentId, @Param("statuses") List<String> statuses);

    /**
     * Check if a refund exists for a payment with any status.
     */
    boolean existsByPaymentId(Long paymentId);

    /**
     * Find pending refund requests (REQUESTED or APPROVED).
     */
    @Query("SELECT r FROM Refund r WHERE r.status IN ('REQUESTED', 'APPROVED') ORDER BY r.requestedAt DESC")
    List<Refund> findPendingRefunds();
}

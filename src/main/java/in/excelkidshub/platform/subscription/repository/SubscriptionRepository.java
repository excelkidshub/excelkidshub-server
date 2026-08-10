package in.excelkidshub.platform.subscription.repository;

import in.excelkidshub.platform.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find subscriptions by user ID.
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * Find active subscriptions by user ID.
     */
    List<Subscription> findByUserIdAndActiveTrue(Long userId);

    /**
     * Find active subscriptions by user ID and status.
     * Returns list to handle multiple active subscriptions gracefully.
     */
    List<Subscription> findByUserIdAndStatusAndActiveTrue(Long userId, String status);

    /**
     * Find subscriptions by plan ID.
     */
    List<Subscription> findByPlanId(Long planId);

    /**
     * Find subscriptions expiring soon.
     */
    @Query("SELECT s FROM Subscription s WHERE s.active = true AND s.status = 'ACTIVE' AND s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find subscription by Razorpay subscription ID.
     */
    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);

    /**
     * Check if user has active subscription.
     */
    boolean existsByUserIdAndStatusAndActiveTrueAndEndDateAfter(Long userId, String status, LocalDate date);
}

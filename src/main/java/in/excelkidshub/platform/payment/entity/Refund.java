package in.excelkidshub.platform.payment.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund entity representing refund requests and their processing status.
 * Tracks the complete refund workflow from request to completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refunds_payment_id", columnList = "payment_id"),
    @Index(name = "idx_refunds_user_id", columnList = "user_id"),
    @Index(name = "idx_refunds_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_refunds_status", columnList = "status"),
    @Index(name = "idx_refunds_requested_at", columnList = "requested_at")
})
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refunds_user"))
    private in.excelkidshub.platform.user.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", foreignKey = @ForeignKey(name = "fk_refunds_subscription"))
    private in.excelkidshub.platform.subscription.entity.Subscription subscription;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "REQUESTED";

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(name = "fk_refunds_reviewed_by"))
    private in.excelkidshub.platform.user.entity.User reviewedBy;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "razorpay_refund_id", length = 255)
    private String razorpayRefundId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", foreignKey = @ForeignKey(name = "fk_refunds_processed_by"))
    private in.excelkidshub.platform.user.entity.User processedBy;

    // Refund status constants
    public static final String STATUS_REQUESTED = "REQUESTED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_REFUNDED = "REFUNDED";
}

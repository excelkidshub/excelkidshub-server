package in.excelkidshub.platform.payment.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing payment transactions.
 * Integrates with Razorpay for payment processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
    @Index(name = "idx_payments_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_razorpay_payment_id", columnList = "razorpay_payment_id"),
    @Index(name = "idx_payments_payment_date", columnList = "payment_date")
})
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", foreignKey = @ForeignKey(name = "fk_payments_subscription"))
    private Subscription subscription;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "razorpay_order_id", length = 255)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 255)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
}

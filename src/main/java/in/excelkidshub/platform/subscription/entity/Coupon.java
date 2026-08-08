package in.excelkidshub.platform.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Coupon entity for discount codes applied at subscription purchase.
 * Maps to the coupons table created in V5 migration.
 * discount_percent and discount_amount are mutually exclusive.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Business helpers ──────────────────────────────────────────────────────

    public boolean isValid() {
        if (!Boolean.TRUE.equals(active)) return false;
        if (maxUses != null && usedCount >= maxUses) return false;
        LocalDate today = LocalDate.now();
        if (validFrom != null && today.isBefore(validFrom)) return false;
        if (validUntil != null && today.isAfter(validUntil)) return false;
        return true;
    }

    /**
     * Calculate the final amount after applying this coupon to the original price.
     * Returns the discounted amount (never negative).
     */
    public BigDecimal applyTo(BigDecimal originalAmount) {
        if (discountPercent != null && discountPercent > 0) {
            BigDecimal discount = originalAmount
                    .multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100));
            return originalAmount.subtract(discount).max(BigDecimal.ZERO);
        }
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return originalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        }
        return originalAmount;
    }
}

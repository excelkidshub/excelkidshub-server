package in.excelkidshub.platform.admin.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Coupon DTO for admin create/update/list operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {

    private Long       id;
    private String     code;
    private Integer    discountPercent;
    private BigDecimal discountAmount;
    private Integer    maxUses;
    private Integer    usedCount;
    private LocalDate  validFrom;
    private LocalDate  validUntil;
    private Boolean    active;
}

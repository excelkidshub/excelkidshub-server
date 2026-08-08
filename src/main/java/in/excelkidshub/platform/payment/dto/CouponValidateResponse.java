package in.excelkidshub.platform.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response from POST /coupons/validate.
 */
@Data
@Builder
public class CouponValidateResponse {

    private Boolean    valid;
    private String     code;
    private Integer    discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private String     message;         // human-readable result e.g. "20% discount applied"
}

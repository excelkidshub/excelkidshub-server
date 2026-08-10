package in.excelkidshub.platform.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for refund response to user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private Long id;
    private Long paymentId;
    private String status;
    private String message;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime processedAt;
    private BigDecimal refundAmount;
}

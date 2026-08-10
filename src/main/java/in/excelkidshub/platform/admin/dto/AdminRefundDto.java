package in.excelkidshub.platform.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for admin refund display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundDto {

    private Long id;
    private Long paymentId;
    private String razorpayOrderId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long subscriptionId;
    private String planName;
    private BigDecimal amount;
    private LocalDateTime purchaseDate;
    private LocalDateTime requestedAt;
    private String requestReason;
    private String status;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String adminNote;
    private BigDecimal refundAmount;
    private String razorpayRefundId;
    private LocalDateTime processedAt;
    private String processedBy;
}

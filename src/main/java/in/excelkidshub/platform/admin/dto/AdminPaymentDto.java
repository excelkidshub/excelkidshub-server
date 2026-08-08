package in.excelkidshub.platform.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPaymentDto {
    private Long        id;
    private Long        userId;
    private String      userEmail;
    private BigDecimal  amount;
    private String      currency;
    private String      status;
    private String      razorpayOrderId;
    private String      razorpayPaymentId;
    private LocalDateTime paymentDate;
    private String      failureReason;
}

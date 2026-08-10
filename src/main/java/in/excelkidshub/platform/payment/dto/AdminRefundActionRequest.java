package in.excelkidshub.platform.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin refund action (approve/reject/mark refunded).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundActionRequest {

    @NotNull(message = "Action is required")
    private String action; // APPROVE, REJECT, MARK_REFUNDED

    private String adminNote;

    private String razorpayRefundId; // Only for MARK_REFUNDED action
}

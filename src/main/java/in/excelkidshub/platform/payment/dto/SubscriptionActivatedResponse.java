package in.excelkidshub.platform.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Response from POST /payments/verify on success.
 * Frontend uses this to update the dashboard subscription card.
 */
@Data
@Builder
public class SubscriptionActivatedResponse {

    private Long      subscriptionId;
    private String    planName;
    private String    status;          // "ACTIVE"
    private LocalDate startDate;
    private LocalDate endDate;
}

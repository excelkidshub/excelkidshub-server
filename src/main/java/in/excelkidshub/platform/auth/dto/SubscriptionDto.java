package in.excelkidshub.platform.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Subscription summary returned inside /auth/me and /subscription/active responses.
 */
@Data
@Builder
public class SubscriptionDto {

    private Long      id;
    private String    planName;
    private String    status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean   active;
    private Long      paymentId; // Added for refund eligibility check
}

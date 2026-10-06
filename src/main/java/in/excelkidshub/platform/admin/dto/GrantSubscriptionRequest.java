package in.excelkidshub.platform.admin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for admin-initiated manual subscription grant.
 * Allows assigning any plan to any user for a custom date range.
 */
@Data
public class GrantSubscriptionRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "planId is required")
    private Long planId;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    /** Optional note visible only in logs (e.g. "Compensation for downtime"). */
    private String note;
}

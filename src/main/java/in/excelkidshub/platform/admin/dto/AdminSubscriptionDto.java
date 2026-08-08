package in.excelkidshub.platform.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminSubscriptionDto {
    private Long      id;
    private Long      userId;
    private String    userEmail;
    private String    userName;
    private Long      planId;
    private String    planName;
    private String    status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}

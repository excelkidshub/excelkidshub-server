package in.excelkidshub.platform.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminStatsDto {
    private Long       totalUsers;
    private Long       activeSubscriptions;
    private Long       expiredSubscriptions;
    private Long       totalPayments;
    private BigDecimal totalRevenue;
    private Long       totalCourses;
    private Long       totalPlans;
}

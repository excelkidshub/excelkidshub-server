package in.excelkidshub.platform.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserDto {
    private Long      id;
    private String    email;
    private String    firstName;
    private String    lastName;
    private String    phone;
    private String    role;
    private Boolean   emailVerified;
    private Boolean   active;
    private LocalDateTime createdAt;
    // subscription summary
    private String    subscriptionStatus;  // ACTIVE | EXPIRED | null
    private String    subscriptionPlan;
}

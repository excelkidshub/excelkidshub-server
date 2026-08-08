package in.excelkidshub.platform.course.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response for GET /courses/{slug}/check.
 * Used by the reading studio auth-guard on every page load.
 */
@Data
@Builder
public class SlugCheckResponse {

    private Boolean allowed;
    private String  reason;   // "NOT_AUTHENTICATED" | "NO_SUBSCRIPTION" | "COURSE_NOT_IN_PLAN" | null
}

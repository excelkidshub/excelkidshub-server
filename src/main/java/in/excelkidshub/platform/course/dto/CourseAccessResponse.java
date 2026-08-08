package in.excelkidshub.platform.course.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response for POST /courses/{id}/access.
 * When allowed=true the frontend redirects to the reading studio with the token.
 */
@Data
@Builder
public class CourseAccessResponse {

    private Boolean allowed;
    private String  redirectToken;  // JWT to pass as ?token= to reading studio
    private String  courseSlug;     // e.g. "phonics/level-1"
    private String  reason;         // "NO_SUBSCRIPTION" | "COURSE_NOT_IN_PLAN" | null
}

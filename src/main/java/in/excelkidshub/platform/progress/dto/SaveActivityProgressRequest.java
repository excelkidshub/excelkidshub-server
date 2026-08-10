package in.excelkidshub.platform.progress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Request body for POST /progress/activity.
 * Called by practice activities and games to record completion.
 */
@Data
public class SaveActivityProgressRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Activity type is required")
    private String activityType;  // PRACTICE, GAME, ASSESSMENT, SONGS

    @NotBlank(message = "Activity ID is required")
    private String activityId;    // e.g. "sound-match-group-1", "word-builder-s"

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must be at most 100")
    private Integer score;        // 0-100, nullable

    @NotNull(message = "Completed status is required")
    private Boolean completed;
}

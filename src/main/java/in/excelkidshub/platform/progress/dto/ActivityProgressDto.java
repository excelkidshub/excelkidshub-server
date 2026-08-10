package in.excelkidshub.platform.progress.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Activity progress DTO returned by activity endpoints.
 */
@Data
@Builder
public class ActivityProgressDto {

    private Long          id;
    private Long          courseId;
    private String        courseTitle;
    private String        courseSlug;
    private String        activityType;    // PRACTICE, GAME, ASSESSMENT, SONGS
    private String        activityId;      // e.g. "sound-match-group-1"
    private Integer       score;           // 0-100
    private Boolean       completed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}

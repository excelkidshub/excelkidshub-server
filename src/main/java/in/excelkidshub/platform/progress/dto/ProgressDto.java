package in.excelkidshub.platform.progress.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Progress summary returned by GET /progress/summary and GET /progress/{courseId}.
 * Used by the dashboard "Continue Reading" card.
 */
@Data
@Builder
public class ProgressDto {

    private Long          id;
    private Long          courseId;
    private String        courseTitle;
    private String        courseSlug;       // e.g. "phonics/level-1" — used to build redirect URL
    private Integer       pageNumber;       // last page visited
    private Integer       totalLessons;     // total pages in course (for percentage calc)
    private Integer       completionPercentage;
    private String        status;           // IN_PROGRESS | COMPLETED
    private LocalDateTime lastAccessed;
}

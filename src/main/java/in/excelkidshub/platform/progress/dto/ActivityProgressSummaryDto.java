package in.excelkidshub.platform.progress.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Activity progress summary returned by GET /progress/activities/summary.
 * Used by the progress page to show activity counts.
 */
@Data
@Builder
public class ActivityProgressSummaryDto {

    private long practiceCount;
    private long gameCount;
    private long assessmentCount;
    private long songsCount;
    private List<ActivityProgressDto> recentActivities;
}

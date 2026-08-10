package in.excelkidshub.platform.progress.service;

import in.excelkidshub.platform.progress.dto.ActivityProgressDto;
import in.excelkidshub.platform.progress.dto.ActivityProgressSummaryDto;
import in.excelkidshub.platform.progress.dto.ProgressDto;
import in.excelkidshub.platform.progress.dto.SaveActivityProgressRequest;
import in.excelkidshub.platform.progress.dto.SaveProgressRequest;

import java.util.List;

public interface ProgressService {

    ProgressDto save(Long userId, SaveProgressRequest request);

    List<ProgressDto> getSummary(Long userId);

    ProgressDto getByCourseId(Long userId, Long courseId);

    // Activity progress methods
    ActivityProgressDto saveActivity(Long userId, SaveActivityProgressRequest request);

    ActivityProgressSummaryDto getActivitySummary(Long userId);
}

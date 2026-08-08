package in.excelkidshub.platform.progress.service;

import in.excelkidshub.platform.progress.dto.ProgressDto;
import in.excelkidshub.platform.progress.dto.SaveProgressRequest;

import java.util.List;

public interface ProgressService {

    ProgressDto save(Long userId, SaveProgressRequest request);

    List<ProgressDto> getSummary(Long userId);

    ProgressDto getByCourseId(Long userId, Long courseId);
}

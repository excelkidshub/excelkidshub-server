package in.excelkidshub.platform.progress.service;

import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.course.entity.Course;
import in.excelkidshub.platform.course.repository.CourseRepository;
import in.excelkidshub.platform.progress.dto.ProgressDto;
import in.excelkidshub.platform.progress.dto.SaveProgressRequest;
import in.excelkidshub.platform.progress.entity.StudentProgress;
import in.excelkidshub.platform.progress.repository.StudentProgressRepository;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Progress tracking — MVP scope:
 *  - Last page visited
 *  - Completion percentage (pageNumber / totalLessons * 100)
 *  - Last accessed timestamp
 *
 * Uses upsert logic: one progress record per (user, course) pair.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED   = "COMPLETED";

    private final StudentProgressRepository progressRepository;
    private final CourseRepository          courseRepository;
    private final UserRepository            userRepository;

    // ── Save / Upsert ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProgressDto save(Long userId, SaveProgressRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Upsert: find existing or create new
        StudentProgress progress = progressRepository
                .findByUserIdAndCourseId(userId, request.getCourseId())
                .orElseGet(() -> {
                    StudentProgress newProgress = StudentProgress.builder()
                            .user(user)
                            .course(course)
                            .completionPercentage(0)
                            .status(STATUS_IN_PROGRESS)
                            .build();
                    newProgress.setActive(true);
                    return newProgress;
                });

        // Only update page if it advances (never go backwards)
        if (progress.getPageNumber() == null || request.getPageNumber() >= progress.getPageNumber()) {
            progress.setPageNumber(request.getPageNumber());
        }

        // Calculate percentage
        int totalPages = course.getTotalLessons() != null && course.getTotalLessons() > 0
                ? course.getTotalLessons() : 1;
        int pct = Math.min(100, (int) Math.round((double) progress.getPageNumber() / totalPages * 100));
        progress.setCompletionPercentage(pct);

        // Mark completed if 100%
        if (pct >= 100) {
            progress.setStatus(STATUS_COMPLETED);
        }

        progress.setLastAccessed(LocalDateTime.now());
        progress = progressRepository.save(progress);

        log.debug("Progress saved: userId={} courseId={} page={} pct={}%",
                userId, request.getCourseId(), progress.getPageNumber(), pct);

        return toDto(progress, course);
    }

    // ── Summary (all courses) ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProgressDto> getSummary(Long userId) {
        return progressRepository.findByUserId(userId)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(p -> toDto(p, p.getCourse()))
                .collect(Collectors.toList());
    }

    // ── Single course ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProgressDto getByCourseId(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        StudentProgress progress = progressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> StudentProgress.builder()
                        .course(course)
                        .pageNumber(0)
                        .completionPercentage(0)
                        .status(STATUS_IN_PROGRESS)
                        .build()
                );

        return toDto(progress, course);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ProgressDto toDto(StudentProgress p, Course course) {
        return ProgressDto.builder()
                .id(p.getId())
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .courseSlug(course != null ? course.getSlug() : null)
                .pageNumber(p.getPageNumber() != null ? p.getPageNumber() : 0)
                .totalLessons(course != null && course.getTotalLessons() != null ? course.getTotalLessons() : 0)
                .completionPercentage(p.getCompletionPercentage() != null ? p.getCompletionPercentage() : 0)
                .status(p.getStatus())
                .lastAccessed(p.getLastAccessed())
                .build();
    }
}

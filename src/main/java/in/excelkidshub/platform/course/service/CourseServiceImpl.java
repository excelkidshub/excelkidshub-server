package in.excelkidshub.platform.course.service;

import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.common.security.JwtUtil;
import in.excelkidshub.platform.course.dto.CourseAccessResponse;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.course.dto.SlugCheckResponse;
import in.excelkidshub.platform.course.entity.Course;
import in.excelkidshub.platform.course.repository.CourseRepository;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.PlanCourseRepository;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Course access logic.
 *
 * Access rules:
 *  1. Free courses (is_free=true) → any authenticated user can access.
 *  2. Paid courses → user must have an ACTIVE subscription whose plan includes the course.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final CourseRepository       courseRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanCourseRepository   planCourseRepository;
    private final UserRepository         userRepository;
    private final JwtUtil                jwtUtil;

    // ── getMyCourses ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses(Long userId) {
        List<Course> result = new ArrayList<>();

        // Always include free courses
        result.addAll(courseRepository.findByIsFreeTrueAndActiveTrue());

        // Add courses from active subscription plan
        findActiveSubscription(userId).ifPresent(sub -> {
            List<Long> planCourseIds = planCourseRepository.findByPlanId(sub.getPlan().getId())
                    .stream()
                    .map(pc -> pc.getCourse().getId())
                    .collect(Collectors.toList());

            if (!planCourseIds.isEmpty()) {
                courseRepository.findByIdInAndActiveTrue(planCourseIds)
                        .forEach(c -> {
                            if (result.stream().noneMatch(r -> r.getId().equals(c.getId()))) {
                                result.add(c);
                            }
                        });
            }
        });

        return result.stream().map(c -> toCourseDto(c, true)).collect(Collectors.toList());
    }

    // ── requestAccess ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CourseAccessResponse requestAccess(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (!Boolean.TRUE.equals(course.getActive())) {
            return CourseAccessResponse.builder().allowed(false).reason("COURSE_NOT_ACTIVE").build();
        }

        // Free course — anyone authenticated can access
        if (Boolean.TRUE.equals(course.getIsFree())) {
            return buildAllowedResponse(userId, course);
        }

        // Paid course — check active subscription
        Optional<Subscription> subOpt = findActiveSubscription(userId);
        if (subOpt.isEmpty()) {
            return CourseAccessResponse.builder().allowed(false).reason("NO_SUBSCRIPTION").build();
        }

        boolean courseInPlan = planCourseRepository
                .existsByPlanIdAndCourseId(subOpt.get().getPlan().getId(), courseId);

        if (!courseInPlan) {
            return CourseAccessResponse.builder().allowed(false).reason("COURSE_NOT_IN_PLAN").build();
        }

        return buildAllowedResponse(userId, course);
    }

    // ── checkBySlug ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SlugCheckResponse checkBySlug(Long userId, String slug) {
        Optional<Course> courseOpt = courseRepository.findBySlugAndActiveTrue(slug);
        if (courseOpt.isEmpty()) {
            // Slug not found — still deny cleanly
            return SlugCheckResponse.builder().allowed(false).reason("COURSE_NOT_FOUND").build();
        }

        Course course = courseOpt.get();

        if (Boolean.TRUE.equals(course.getIsFree())) {
            return SlugCheckResponse.builder().allowed(true).build();
        }

        Optional<Subscription> subOpt = findActiveSubscription(userId);
        if (subOpt.isEmpty()) {
            return SlugCheckResponse.builder().allowed(false).reason("NO_SUBSCRIPTION").build();
        }

        boolean courseInPlan = planCourseRepository
                .existsByPlanIdAndCourseId(subOpt.get().getPlan().getId(), course.getId());

        if (!courseInPlan) {
            return SlugCheckResponse.builder().allowed(false).reason("COURSE_NOT_IN_PLAN").build();
        }

        return SlugCheckResponse.builder().allowed(true).build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Optional<Subscription> findActiveSubscription(Long userId) {
        return subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(userId, STATUS_ACTIVE)
                .filter(s -> s.getEndDate() != null && !s.getEndDate().isBefore(LocalDate.now()));
    }

    private CourseAccessResponse buildAllowedResponse(Long userId, Course course) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Issue a fresh JWT for the reading studio redirect
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );

        return CourseAccessResponse.builder()
                .allowed(true)
                .redirectToken(token)
                .courseSlug(course.getSlug())
                .build();
    }

    private CourseDto toCourseDto(Course c, boolean hasAccess) {
        return CourseDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .levelName(c.getLevelName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .ageGroup(c.getAgeGroup())
                .thumbnailUrl(c.getThumbnailUrl())
                .totalLessons(c.getTotalLessons())
                .isFree(c.getIsFree())
                .hasAccess(hasAccess)
                .build();
    }
}

package in.excelkidshub.platform.course.service;

import in.excelkidshub.platform.course.dto.CourseAccessResponse;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.course.dto.SlugCheckResponse;

import java.util.List;

/**
 * Course service contract.
 */
public interface CourseService {

    /** All courses visible to this user (free + subscribed). */
    List<CourseDto> getMyCourses(Long userId);

    /** Validate access and return a redirect token for the reading studio. */
    CourseAccessResponse requestAccess(Long userId, Long courseId);

    /** Check if userId can access a course identified by its URL slug. */
    SlugCheckResponse checkBySlug(Long userId, String slug);
}

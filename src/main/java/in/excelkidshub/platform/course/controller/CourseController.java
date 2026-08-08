package in.excelkidshub.platform.course.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.course.dto.CourseAccessResponse;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.course.dto.SlugCheckResponse;
import in.excelkidshub.platform.course.service.CourseService;
import in.excelkidshub.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Course access endpoints.
 *
 * GET  /courses/my              — courses available to the authenticated user
 * POST /courses/{id}/access     — verify access, get redirect token for reading studio
 * GET  /courses/{slug}/check    — auth-guard check used by reading studio on page load
 *
 * Note: {slug} path variables contain slashes (e.g. "phonics/level-1").
 * Spring handles this via AntPathMatcher when the variable is declared as "**" pattern.
 * We use a dedicated mapping for slug to avoid conflicts with the numeric {id} route.
 */
@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ── My Courses ────────────────────────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getMyCourses(
            @AuthenticationPrincipal User currentUser) {

        List<CourseDto> courses = courseService.getMyCourses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Courses loaded", courses));
    }

    // ── Request Access (by ID) ────────────────────────────────────────────────

    @PostMapping("/{id}/access")
    public ResponseEntity<ApiResponse<CourseAccessResponse>> requestAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        CourseAccessResponse response = courseService.requestAccess(currentUser.getId(), id);
        String message = Boolean.TRUE.equals(response.getAllowed()) ? "Access granted" : "Access denied";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ── Slug Check (reading studio auth-guard) ────────────────────────────────

    /**
     * Used by auth-guard.js on every page load in the reading studio.
     * The slug may contain slashes — mapped as a wildcard path.
     * Example: GET /courses/phonics/level-1/check
     */
    @GetMapping("/**/check")
    public ResponseEntity<ApiResponse<SlugCheckResponse>> checkBySlug(
            @AuthenticationPrincipal User currentUser,
            jakarta.servlet.http.HttpServletRequest request) {

        // Extract slug from the path: strip "/courses/" prefix and "/check" suffix
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();    // "/api"
        // path = /api/courses/phonics/level-1/check
        String stripped = path.replace(contextPath + "/courses/", "").replace("/check", "");

        SlugCheckResponse response = courseService.checkBySlug(currentUser.getId(), stripped);
        String message = Boolean.TRUE.equals(response.getAllowed()) ? "Access granted" : "Access denied";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}

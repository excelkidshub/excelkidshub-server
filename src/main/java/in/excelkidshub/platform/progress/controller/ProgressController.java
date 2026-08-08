package in.excelkidshub.platform.progress.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.progress.dto.ProgressDto;
import in.excelkidshub.platform.progress.dto.SaveProgressRequest;
import in.excelkidshub.platform.progress.service.ProgressService;
import in.excelkidshub.platform.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Progress tracking endpoints.
 *
 * POST /progress/save          — called by reading studio on every page load
 * GET  /progress/summary       — all courses progress, used by dashboard
 * GET  /progress/{courseId}    — progress for a single course
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<ProgressDto>> save(
            @Valid @RequestBody SaveProgressRequest request,
            @AuthenticationPrincipal User currentUser) {

        ProgressDto dto = progressService.save(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Progress saved", dto));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<ProgressDto>>> getSummary(
            @AuthenticationPrincipal User currentUser) {

        List<ProgressDto> summary = progressService.getSummary(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Progress summary loaded", summary));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<ProgressDto>> getByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {

        ProgressDto dto = progressService.getByCourseId(currentUser.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Progress loaded", dto));
    }
}

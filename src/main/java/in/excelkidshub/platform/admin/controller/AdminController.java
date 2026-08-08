package in.excelkidshub.platform.admin.controller;

import in.excelkidshub.platform.admin.dto.*;
import in.excelkidshub.platform.admin.service.AdminService;
import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.common.dto.PageResponse;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.payment.dto.PlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only REST API.
 * All endpoints require ADMIN role — enforced by Spring Security @PreAuthorize.
 * SecurityConfig also has .requestMatchers("/admin/**").hasRole("ADMIN") as a belt-and-suspenders.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats loaded", adminService.getStats()));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<AdminUserDto>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(ApiResponse.success("Users loaded",
                PageResponse.of(adminService.getUsers(search, pageable))));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User loaded", adminService.getUserById(id)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        adminService.setUserStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(active ? "User activated" : "User deactivated"));
    }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<PageResponse<AdminSubscriptionDto>>> getSubscriptions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(ApiResponse.success("Subscriptions loaded",
                PageResponse.of(adminService.getSubscriptions(status, pageable))));
    }

    @PutMapping("/subscriptions/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setSubscriptionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        adminService.setSubscriptionStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Subscription status updated"));
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<AdminPaymentDto>>> getPayments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(ApiResponse.success("Payments loaded",
                PageResponse.of(adminService.getPayments(status, pageable))));
    }

    // ── Plans ─────────────────────────────────────────────────────────────────

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<PageResponse<PlanDto>>> getPlans(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Plans loaded",
                PageResponse.of(adminService.getPlans(PageRequest.of(page, size)))));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<PlanDto>> createPlan(@RequestBody PlanDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plan created", adminService.createPlan(dto)));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<PlanDto>> updatePlan(
            @PathVariable Long id, @RequestBody PlanDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Plan updated", adminService.updatePlan(id, dto)));
    }

    // ── Coupons ───────────────────────────────────────────────────────────────

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<PageResponse<CouponDto>>> getCoupons(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(ApiResponse.success("Coupons loaded",
                PageResponse.of(adminService.getCoupons(PageRequest.of(page, size)))));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CouponDto>> createCoupon(@RequestBody CouponDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created", adminService.createCoupon(dto)));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponDto>> updateCoupon(
            @PathVariable Long id, @RequestBody CouponDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Coupon updated", adminService.updateCoupon(id, dto)));
    }

    @PutMapping("/coupons/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setCouponStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        adminService.setCouponStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(active ? "Coupon activated" : "Coupon deactivated"));
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<PageResponse<CourseDto>>> getCourses(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success("Courses loaded",
                PageResponse.of(adminService.getCourses(PageRequest.of(page, size)))));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@RequestBody CourseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created", adminService.createCourse(dto)));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(
            @PathVariable Long id, @RequestBody CourseDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Course updated", adminService.updateCourse(id, dto)));
    }

    @PutMapping("/courses/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setCourseStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        adminService.setCourseStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(active ? "Course activated" : "Course deactivated"));
    }
}

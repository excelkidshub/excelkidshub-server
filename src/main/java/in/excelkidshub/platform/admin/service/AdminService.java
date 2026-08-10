package in.excelkidshub.platform.admin.service;

import in.excelkidshub.platform.admin.dto.*;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.payment.dto.AdminRefundActionRequest;
import in.excelkidshub.platform.payment.dto.PlanDto;
import in.excelkidshub.platform.payment.dto.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    AdminStatsDto getStats();

    Page<AdminUserDto> getUsers(String search, Pageable pageable);
    AdminUserDto getUserById(Long id);
    void setUserStatus(Long id, boolean active);

    Page<AdminSubscriptionDto> getSubscriptions(String status, Pageable pageable);
    void setSubscriptionStatus(Long id, String status);

    Page<AdminPaymentDto> getPayments(String status, Pageable pageable);

    Page<CouponDto> getCoupons(Pageable pageable);
    CouponDto createCoupon(CouponDto dto);
    CouponDto updateCoupon(Long id, CouponDto dto);
    void setCouponStatus(Long id, boolean active);

    Page<PlanDto> getPlans(Pageable pageable);
    PlanDto createPlan(PlanDto dto);
    PlanDto updatePlan(Long id, PlanDto dto);

    Page<CourseDto> getCourses(Pageable pageable);
    CourseDto createCourse(CourseDto dto);
    CourseDto updateCourse(Long id, CourseDto dto);
    void setCourseStatus(Long id, boolean active);

    // Refund management
    List<AdminRefundDto> getRefundRequests(String status);
    List<AdminRefundDto> getPendingRefunds();
    AdminRefundDto getRefundById(Long id);
    RefundResponse processRefundAction(Long refundId, Long adminUserId, AdminRefundActionRequest request);
}

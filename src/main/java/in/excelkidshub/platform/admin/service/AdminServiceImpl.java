package in.excelkidshub.platform.admin.service;

import in.excelkidshub.platform.admin.dto.*;
import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.course.dto.CourseDto;
import in.excelkidshub.platform.course.entity.Course;
import in.excelkidshub.platform.course.repository.CourseRepository;
import in.excelkidshub.platform.payment.dto.AdminRefundActionRequest;
import in.excelkidshub.platform.payment.dto.PlanDto;
import in.excelkidshub.platform.payment.dto.RefundResponse;
import in.excelkidshub.platform.payment.entity.Payment;
import in.excelkidshub.platform.payment.entity.Refund;
import in.excelkidshub.platform.payment.repository.PaymentRepository;
import in.excelkidshub.platform.payment.repository.RefundRepository;
import in.excelkidshub.platform.payment.service.RefundService;
import in.excelkidshub.platform.subscription.entity.Coupon;
import in.excelkidshub.platform.subscription.entity.Plan;
import in.excelkidshub.platform.subscription.entity.Subscription;
import in.excelkidshub.platform.subscription.repository.CouponRepository;
import in.excelkidshub.platform.subscription.repository.PlanRepository;
import in.excelkidshub.platform.subscription.repository.SubscriptionRepository;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository         userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository      paymentRepository;
    private final PlanRepository         planRepository;
    private final CourseRepository       courseRepository;
    private final CouponRepository       couponRepository;
    private final RefundRepository       refundRepository;
    private final RefundService          refundService;

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        long totalUsers   = userRepository.count();
        long activeSubs   = subscriptionRepository.findAll().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus())
                        && s.getEndDate() != null
                        && !s.getEndDate().isBefore(LocalDate.now()))
                .count();
        long expiredSubs  = subscriptionRepository.findAll().stream()
                .filter(s -> "EXPIRED".equals(s.getStatus())).count();
        long totalPayments = paymentRepository.count();
        BigDecimal revenue = paymentRepository.findByStatus("SUCCESS").stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .activeSubscriptions(activeSubs)
                .expiredSubscriptions(expiredSubs)
                .totalPayments(totalPayments)
                .totalRevenue(revenue)
                .totalCourses(courseRepository.count())
                .totalPlans(planRepository.count())
                .build();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDto> getUsers(String search, Pageable pageable) {
        List<User> all = userRepository.findAll();

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream()
                    .filter(u -> (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                            || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q))
                            || (u.getLastName()  != null && u.getLastName().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), all.size());
        List<AdminUserDto> page = all.subList(start, end).stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());

        return new PageImpl<>(page, pageable, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toUserDto(user);
    }

    @Override
    @Transactional
    public void setUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(active);
        userRepository.save(user);
        log.info("Admin: user {} set active={}", id, active);
    }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminSubscriptionDto> getSubscriptions(String status, Pageable pageable) {
        List<Subscription> all = (status != null && !status.isBlank())
                ? subscriptionRepository.findAll().stream()
                        .filter(s -> status.equalsIgnoreCase(s.getStatus()))
                        .collect(Collectors.toList())
                : subscriptionRepository.findAll();

        // Sort newest first
        all.sort((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0);

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), all.size());
        List<AdminSubscriptionDto> page = all.subList(start, end).stream()
                .map(this::toSubDto)
                .collect(Collectors.toList());

        return new PageImpl<>(page, pageable, all.size());
    }

    @Override
    @Transactional
    public void setSubscriptionStatus(Long id, String status) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
        sub.setStatus(status.toUpperCase());
        subscriptionRepository.save(sub);
        log.info("Admin: subscription {} set status={}", id, status);
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPaymentDto> getPayments(String status, Pageable pageable) {
        List<Payment> all = (status != null && !status.isBlank())
                ? paymentRepository.findByStatus(status.toUpperCase())
                : paymentRepository.findAll();

        all.sort((a, b) -> b.getCreatedAt() != null && a.getCreatedAt() != null
                ? b.getCreatedAt().compareTo(a.getCreatedAt()) : 0);

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), all.size());
        List<AdminPaymentDto> page = all.subList(start, end).stream()
                .map(this::toPaymentDto)
                .collect(Collectors.toList());

        return new PageImpl<>(page, pageable, all.size());
    }

    // ── Coupons ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CouponDto> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(this::toCouponDto);
    }

    @Override
    @Transactional
    public CouponDto createCoupon(CouponDto dto) {
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().trim().toUpperCase())
                .discountPercent(dto.getDiscountPercent())
                .discountAmount(dto.getDiscountAmount())
                .maxUses(dto.getMaxUses())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        return toCouponDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponDto updateCoupon(Long id, CouponDto dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        if (dto.getDiscountPercent() != null) coupon.setDiscountPercent(dto.getDiscountPercent());
        if (dto.getDiscountAmount()  != null) coupon.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getMaxUses()         != null) coupon.setMaxUses(dto.getMaxUses());
        if (dto.getValidFrom()       != null) coupon.setValidFrom(dto.getValidFrom());
        if (dto.getValidUntil()      != null) coupon.setValidUntil(dto.getValidUntil());
        if (dto.getActive()          != null) coupon.setActive(dto.getActive());
        return toCouponDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void setCouponStatus(Long id, boolean active) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.setActive(active);
        couponRepository.save(coupon);
        log.info("Admin: coupon {} set active={}", id, active);
    }

    // ── Plans ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PlanDto> getPlans(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::toPlanDto);
    }

    @Override
    @Transactional
    public PlanDto createPlan(PlanDto dto) {
        Plan plan = Plan.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .durationMonths(dto.getDurationMonths())
                .isPopular(Boolean.TRUE.equals(dto.getIsPopular()))
                .features(dto.getFeatures())
                .build();
        plan.setActive(true);
        return toPlanDto(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanDto updatePlan(Long id, PlanDto dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        if (dto.getName()          != null) plan.setName(dto.getName());
        if (dto.getDescription()   != null) plan.setDescription(dto.getDescription());
        if (dto.getPrice()         != null) plan.setPrice(dto.getPrice());
        if (dto.getDurationMonths()!= null) plan.setDurationMonths(dto.getDurationMonths());
        if (dto.getIsPopular()     != null) plan.setIsPopular(dto.getIsPopular());
        if (dto.getFeatures()      != null) plan.setFeatures(dto.getFeatures());
        return toPlanDto(planRepository.save(plan));
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> getCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(c -> toCourseDto(c, true));
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto dto) {
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .levelName(dto.getLevelName() != null ? dto.getLevelName() : "")
                .ageGroup(dto.getAgeGroup())
                .difficultyLevel("Beginner")
                .slug(dto.getSlug())
                .thumbnailUrl(dto.getThumbnailUrl())
                .totalLessons(dto.getTotalLessons())
                .isFree(Boolean.TRUE.equals(dto.getIsFree()))
                .build();
        course.setActive(true);
        return toCourseDto(courseRepository.save(course), true);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        if (dto.getTitle()       != null) course.setTitle(dto.getTitle());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getLevelName()   != null) course.setLevelName(dto.getLevelName());
        if (dto.getSlug()        != null) course.setSlug(dto.getSlug());
        if (dto.getTotalLessons()!= null) course.setTotalLessons(dto.getTotalLessons());
        if (dto.getIsFree()      != null) course.setIsFree(dto.getIsFree());
        return toCourseDto(courseRepository.save(course), true);
    }

    @Override
    @Transactional
    public void setCourseStatus(Long id, boolean active) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        course.setActive(active);
        courseRepository.save(course);
        log.info("Admin: course {} set active={}", id, active);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private AdminUserDto toUserDto(User u) {
        String subStatus = null, subPlan = null;
        var activeSubs = subscriptionRepository
                .findByUserIdAndStatusAndActiveTrue(u.getId(), "ACTIVE");
        if (!activeSubs.isEmpty()) {
            // Pick the most recent subscription by start date
            var activeSub = activeSubs.stream()
                    .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .orElse(activeSubs.get(0));
            subStatus = activeSub.getStatus();
            subPlan   = activeSub.getPlan() != null ? activeSub.getPlan().getName() : null;
        }
        return AdminUserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phone(u.getPhone())
                .role(u.getRole() != null ? u.getRole().getName() : null)
                .emailVerified(u.getEmailVerified())
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .subscriptionStatus(subStatus)
                .subscriptionPlan(subPlan)
                .build();
    }

    private AdminSubscriptionDto toSubDto(Subscription s) {
        return AdminSubscriptionDto.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .userEmail(s.getUser() != null ? s.getUser().getEmail() : null)
                .userName(s.getUser() != null
                        ? s.getUser().getFirstName() + " " + s.getUser().getLastName() : null)
                .planId(s.getPlan() != null ? s.getPlan().getId() : null)
                .planName(s.getPlan() != null ? s.getPlan().getName() : null)
                .status(s.getStatus())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private AdminPaymentDto toPaymentDto(Payment p) {
        return AdminPaymentDto.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .userEmail(p.getUser() != null ? p.getUser().getEmail() : null)
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .paymentDate(p.getPaymentDate())
                .failureReason(p.getFailureReason())
                .build();
    }

    private CouponDto toCouponDto(Coupon c) {
        return CouponDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .discountPercent(c.getDiscountPercent())
                .discountAmount(c.getDiscountAmount())
                .maxUses(c.getMaxUses())
                .usedCount(c.getUsedCount())
                .validFrom(c.getValidFrom())
                .validUntil(c.getValidUntil())
                .active(c.getActive())
                .build();
    }

    private PlanDto toPlanDto(Plan p) {
        return PlanDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .durationMonths(p.getDurationMonths())
                .isPopular(p.getIsPopular())
                .features(p.getFeatures())
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

    // ── Refunds ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AdminRefundDto> getRefundRequests(String status) {
        List<Refund> refunds = (status != null && !status.isBlank())
                ? refundRepository.findByStatus(status.toUpperCase())
                : refundRepository.findAll();
        
        // Sort newest first
        refunds.sort((a, b) -> b.getRequestedAt() != null && a.getRequestedAt() != null
                ? b.getRequestedAt().compareTo(a.getRequestedAt()) : 0);
        
        return refunds.stream().map(this::toRefundDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRefundDto> getPendingRefunds() {
        List<Refund> refunds = refundRepository.findPendingRefunds();
        return refunds.stream().map(this::toRefundDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRefundDto getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", id));
        return toRefundDto(refund);
    }

    @Override
    @Transactional
    public RefundResponse processRefundAction(Long refundId, Long adminUserId, AdminRefundActionRequest request) {
        return refundService.processRefundAction(refundId, adminUserId, request);
    }

    private AdminRefundDto toRefundDto(Refund r) {
        return AdminRefundDto.builder()
                .id(r.getId())
                .paymentId(r.getPayment() != null ? r.getPayment().getId() : null)
                .razorpayOrderId(r.getPayment() != null ? r.getPayment().getRazorpayOrderId() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(r.getUser() != null 
                        ? r.getUser().getFirstName() + " " + r.getUser().getLastName() : null)
                .userEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .subscriptionId(r.getSubscription() != null ? r.getSubscription().getId() : null)
                .planName(r.getSubscription() != null && r.getSubscription().getPlan() != null 
                        ? r.getSubscription().getPlan().getName() : null)
                .amount(r.getPayment() != null ? r.getPayment().getAmount() : null)
                .purchaseDate(r.getPayment() != null ? r.getPayment().getPaymentDate() : null)
                .requestedAt(r.getRequestedAt())
                .requestReason(r.getRequestReason())
                .status(r.getStatus())
                .reviewedAt(r.getReviewedAt())
                .reviewedBy(r.getReviewedBy() != null 
                        ? r.getReviewedBy().getFirstName() + " " + r.getReviewedBy().getLastName() : null)
                .adminNote(r.getAdminNote())
                .refundAmount(r.getRefundAmount())
                .razorpayRefundId(r.getRazorpayRefundId())
                .processedAt(r.getProcessedAt())
                .processedBy(r.getProcessedBy() != null 
                        ? r.getProcessedBy().getFirstName() + " " + r.getProcessedBy().getLastName() : null)
                .build();
    }
}

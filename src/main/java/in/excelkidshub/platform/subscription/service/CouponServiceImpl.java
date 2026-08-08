package in.excelkidshub.platform.subscription.service;

import in.excelkidshub.platform.common.exception.ResourceNotFoundException;
import in.excelkidshub.platform.payment.dto.CouponValidateRequest;
import in.excelkidshub.platform.payment.dto.CouponValidateResponse;
import in.excelkidshub.platform.subscription.entity.Coupon;
import in.excelkidshub.platform.subscription.entity.Plan;
import in.excelkidshub.platform.subscription.repository.CouponRepository;
import in.excelkidshub.platform.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final PlanRepository   planRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidateResponse validate(CouponValidateRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));

        BigDecimal originalAmount = plan.getPrice();

        Optional<Coupon> couponOpt = couponRepository
                .findByCodeIgnoreCase(request.getCode().trim());

        // Coupon not found
        if (couponOpt.isEmpty()) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .code(request.getCode())
                    .originalAmount(originalAmount)
                    .finalAmount(originalAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Invalid coupon code.")
                    .build();
        }

        Coupon coupon = couponOpt.get();

        // Coupon found but not valid (expired / exhausted / inactive)
        if (!coupon.isValid()) {
            return CouponValidateResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .originalAmount(originalAmount)
                    .finalAmount(originalAmount)
                    .discountAmount(BigDecimal.ZERO)
                    .message("This coupon is expired or no longer available.")
                    .build();
        }

        BigDecimal finalAmount    = coupon.applyTo(originalAmount);
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);

        String message = coupon.getDiscountPercent() != null
                ? coupon.getDiscountPercent() + "% discount applied"
                : "₹" + discountAmount.toPlainString() + " discount applied";

        log.info("Coupon {} validated for plan {}: discount={}", coupon.getCode(), plan.getName(), discountAmount);

        return CouponValidateResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .discountPercent(coupon.getDiscountPercent())
                .discountAmount(discountAmount)
                .originalAmount(originalAmount)
                .finalAmount(finalAmount)
                .message(message)
                .build();
    }
}

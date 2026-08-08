package in.excelkidshub.platform.subscription.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.payment.dto.CouponValidateRequest;
import in.excelkidshub.platform.payment.dto.CouponValidateResponse;
import in.excelkidshub.platform.subscription.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /coupons/validate — validate a coupon code and return the discounted price.
 * Called by the pricing page before opening Razorpay Checkout.
 * Requires authentication (user must be logged in to purchase).
 */
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidateResponse>> validate(
            @Valid @RequestBody CouponValidateRequest request) {

        CouponValidateResponse response = couponService.validate(request);
        String message = Boolean.TRUE.equals(response.getValid())
                ? "Coupon applied: " + response.getMessage()
                : response.getMessage();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}

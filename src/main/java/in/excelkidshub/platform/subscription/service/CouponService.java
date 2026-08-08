package in.excelkidshub.platform.subscription.service;

import in.excelkidshub.platform.payment.dto.CouponValidateRequest;
import in.excelkidshub.platform.payment.dto.CouponValidateResponse;

public interface CouponService {
    CouponValidateResponse validate(CouponValidateRequest request);
}

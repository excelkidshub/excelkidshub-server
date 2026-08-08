package in.excelkidshub.platform.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Plan summary returned by GET /plans.
 */
@Data
@Builder
public class PlanDto {

    private Long               id;
    private String             name;
    private String             description;
    private BigDecimal         price;
    private Integer            durationMonths;
    private Boolean            isPopular;
    private Map<String, Object> features;
}

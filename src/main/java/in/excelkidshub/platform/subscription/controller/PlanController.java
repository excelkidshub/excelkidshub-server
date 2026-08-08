package in.excelkidshub.platform.subscription.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.payment.dto.PlanDto;
import in.excelkidshub.platform.subscription.entity.Plan;
import in.excelkidshub.platform.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public endpoint — returns all active plans for the pricing page.
 * No authentication required.
 */
@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanDto>>> getPlans() {
        List<PlanDto> plans = planRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Plans loaded", plans));
    }

    private PlanDto toDto(Plan p) {
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
}

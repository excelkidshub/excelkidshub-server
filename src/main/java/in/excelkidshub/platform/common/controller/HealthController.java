package in.excelkidshub.platform.common.controller;

import in.excelkidshub.platform.common.dto.ApiResponse;
import in.excelkidshub.platform.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring and deployment.
 * Used by Kubernetes, Oracle Cloud, and other orchestration tools.
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final RoleRepository roleRepository;

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        log.debug("Health check requested");
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ApiResponse.success("Service is healthy", status);
    }

    @GetMapping("/db")
    public ApiResponse<Map<String, Object>> databaseHealth() {
        log.debug("Database health check requested");
        Map<String, Object> status = new HashMap<>();
        try {
            long roleCount = roleRepository.count();
            status.put("status", "UP");
            status.put("database", "Connected");
            status.put("roleCount", roleCount);
            status.put("message", "Database connection successful");
            return ApiResponse.success("Database is healthy", status);
        } catch (Exception e) {
            log.error("Database health check failed", e);
            status.put("status", "DOWN");
            status.put("database", "Disconnected");
            status.put("error", e.getMessage());
            return ApiResponse.error("Database connection failed", null);
        }
    }
}

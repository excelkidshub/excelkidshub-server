package in.excelkidshub.platform.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for the application.
 * Enables JPA auditing for automatic timestamp management.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

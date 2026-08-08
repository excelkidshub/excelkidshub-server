package in.excelkidshub.platform.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration properties.
 * Centralizes all custom application properties bound from application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Razorpay razorpay = new Razorpay();
    private Frontend frontend = new Frontend();
    private Mail mail = new Mail();

    @Data
    public static class Razorpay {
        private String keyId;
        private String keySecret;
        private String webhookSecret;
    }

    @Data
    public static class Frontend {
        private String mainUrl = "https://excelkidshub.in";
        private String readUrl = "https://read.excelkidshub.in";
    }

    @Data
    public static class Mail {
        private String from = "noreply@excelkidshub.in";
        private String fromName = "ExcelKidsHub";
    }
}

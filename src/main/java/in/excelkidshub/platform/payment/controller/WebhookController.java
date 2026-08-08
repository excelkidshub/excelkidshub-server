package in.excelkidshub.platform.payment.controller;

import in.excelkidshub.platform.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Razorpay webhook receiver.
 *
 * Mapped under /webhooks/** which is in the SecurityConfig permitAll list.
 * Must receive the raw request body (not parsed by Spring) for HMAC verification.
 * Always returns 200 immediately — Razorpay retries on any non-2xx response.
 */
@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Razorpay webhook received, signature present={}", signature != null);

        if (signature == null || signature.isBlank()) {
            log.warn("Razorpay webhook missing signature — ignoring");
            return ResponseEntity.ok("ok");
        }

        try {
            paymentService.handleWebhook(rawBody, signature);
        } catch (Exception e) {
            // Never return non-2xx — Razorpay would retry infinitely
            log.error("Webhook processing error (non-fatal): {}", e.getMessage());
        }

        return ResponseEntity.ok("ok");
    }
}

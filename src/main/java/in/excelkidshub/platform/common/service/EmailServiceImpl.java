package in.excelkidshub.platform.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of email service using JavaMailSender.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@excelkidshub.com}")
    private String fromEmail;

    @Value("${app.mail.admin:excelkidshub.edu@gmail.com}")
    private String adminEmail;

    @Override
    public void sendRefundRequestNotification(String userEmail, String userName, Long paymentId, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("New Refund Request - ExcelKidsHub");
            message.setText(String.format(
                "A new refund request has been submitted:\n\n" +
                "User: %s (%s)\n" +
                "Payment ID: %d\n" +
                "Reason: %s\n\n" +
                "Please review this request in the admin portal.",
                userName, userEmail, paymentId, reason != null ? reason : "No reason provided"
            ));
            mailSender.send(message);
            log.info("Refund request notification sent to admin for paymentId={}", paymentId);
        } catch (Exception e) {
            log.error("Failed to send refund request notification for paymentId={}: {}", paymentId, e.getMessage());
        }
    }

    @Override
    public void sendRefundApprovedNotification(String userEmail, String userName, String refundAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Refund Request Approved - ExcelKidsHub");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your refund request has been approved.\n" +
                "Refund Amount: %s\n\n" +
                "The refund will be processed to your original payment method within 5-7 business days.\n\n" +
                "Thank you for your patience.\n\n" +
                "Best regards,\n" +
                "ExcelKidsHub Team",
                userName, refundAmount
            ));
            mailSender.send(message);
            log.info("Refund approved notification sent to userEmail={}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send refund approved notification to userEmail={}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendRefundRejectedNotification(String userEmail, String userName, String adminNote) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Refund Request Update - ExcelKidsHub");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your refund request has been reviewed and could not be approved at this time.\n\n" +
                "%s\n\n" +
                "If you have any questions, please contact our support team at excelkidshub.edu@gmail.com or +91 8793135679.\n\n" +
                "Best regards,\n" +
                "ExcelKidsHub Team",
                userName, adminNote != null ? adminNote : "Please contact support for more details."
            ));
            mailSender.send(message);
            log.info("Refund rejected notification sent to userEmail={}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send refund rejected notification to userEmail={}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendRefundProcessedNotification(String userEmail, String userName, String refundAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Refund Processed - ExcelKidsHub");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your refund of %s has been successfully processed.\n\n" +
                "Your digital subscription access has ended as of this date.\n\n" +
                "Thank you for being part of ExcelKidsHub. We hope to see you again soon!\n\n" +
                "Best regards,\n" +
                "ExcelKidsHub Team",
                userName, refundAmount
            ));
            mailSender.send(message);
            log.info("Refund processed notification sent to userEmail={}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send refund processed notification to userEmail={}: {}", userEmail, e.getMessage());
        }
    }

    @Override
    public void sendSubscriptionCancellationNotification(String userEmail, String userName, String planName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Subscription Cancelled - ExcelKidsHub");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your subscription to %s has been cancelled as per your request.\n\n" +
                "Your access to the digital content will continue until the end of your current billing period.\n\n" +
                "Thank you for being part of ExcelKidsHub. We hope to see you again soon!\n\n" +
                "Best regards,\n" +
                "ExcelKidsHub Team",
                userName, planName
            ));
            mailSender.send(message);
            log.info("Subscription cancellation notification sent to userEmail={}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send subscription cancellation notification to userEmail={}: {}", userEmail, e.getMessage());
        }
    }
}

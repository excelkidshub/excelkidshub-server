package in.excelkidshub.platform.common.service;

/**
 * Service for sending email notifications.
 */
public interface EmailService {

    /**
     * Send refund request notification to admin.
     */
    void sendRefundRequestNotification(String userEmail, String userName, Long paymentId, String reason);

    /**
     * Send refund approved notification to customer.
     */
    void sendRefundApprovedNotification(String userEmail, String userName, String refundAmount);

    /**
     * Send refund rejected notification to customer.
     */
    void sendRefundRejectedNotification(String userEmail, String userName, String adminNote);

    /**
     * Send refund processed notification to customer.
     */
    void sendRefundProcessedNotification(String userEmail, String userName, String refundAmount);

    /**
     * Send subscription cancellation notification to customer.
     */
    void sendSubscriptionCancellationNotification(String userEmail, String userName, String planName);
}

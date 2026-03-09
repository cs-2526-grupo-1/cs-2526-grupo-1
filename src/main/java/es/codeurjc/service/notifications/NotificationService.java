package es.codeurjc.service.notifications;

import es.codeurjc.model.User;
import es.codeurjc.model.Notification;

/**
 * Interface for notification services.
 * Implementations will send notifications via different channels.
 */
public interface NotificationService {
    
    /**
     * Send notification to user
     * @param user the user
     * @param type notification type
     * @param subject notification subject
     * @param message notification message
     */
    void sendNotification(User user, Notification.NotificationType type, 
                         String subject, String message);
    
    /**
     * Get the notification channel for this service
     * @return the notification channel
     */
    Notification.NotificationChannel getChannel();
}

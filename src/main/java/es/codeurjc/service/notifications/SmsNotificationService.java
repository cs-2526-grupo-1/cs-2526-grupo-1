package es.codeurjc.service.notifications;

import es.codeurjc.model.User;
import es.codeurjc.model.Notification;
import es.codeurjc.repository.NotificationRepository;

import org.springframework.stereotype.Service;

/**
 * SMS notification service implementation.
 */
@Service
public class SmsNotificationService implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public SmsNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public void sendNotification(User user, Notification.NotificationType type,
                                String subject, String message) {
        // Create notification record
        Notification notification = new Notification(
            user,
            type,
            Notification.NotificationChannel.SMS,
            user.getPhone(),
            subject,
            message
        );
        
        // In real implementation, would send SMS here
        // For this practice, just log it
        System.out.println("Sending SMS to: " + user.getPhone());
        System.out.println("Message: " + message);
        
        notification.setSent(true);
        notificationRepository.save(notification);
    }
    
    @Override
    public Notification.NotificationChannel getChannel() {
        return Notification.NotificationChannel.SMS;
    }
}

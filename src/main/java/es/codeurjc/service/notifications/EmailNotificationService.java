package es.codeurjc.service.notifications;

import es.codeurjc.model.User;
import es.codeurjc.model.Notification;
import es.codeurjc.repository.NotificationRepository;

import org.springframework.stereotype.Service;

/**
 * Email notification service implementation.
 */
@Service
public class EmailNotificationService implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public EmailNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public void sendNotification(User user, Notification.NotificationType type,
                                String subject, String message) {
        // Create notification record
        Notification notification = new Notification(
            user,
            type,
            Notification.NotificationChannel.EMAIL,
            user.getEmail(),
            subject,
            message
        );
        
        // In real implementation, would send email here
        // For this practice, just log it
        System.out.println("Sending EMAIL to: " + user.getEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        
        notification.setSent(true);
        notificationRepository.save(notification);
    }
    
    @Override
    public Notification.NotificationChannel getChannel() {
        return Notification.NotificationChannel.EMAIL;
    }
}

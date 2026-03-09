package es.codeurjc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification entity for tracking sent notifications.
 */
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    
    private String recipient; // email or phone number
    private String subject;
    private String message;
    
    private LocalDateTime sentAt;
    private boolean sent;
    
    public enum NotificationType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        LOAN_APPROVED,
        LOAN_REJECTED,
        LOAN_PAYMENT
    }
    
    public enum NotificationChannel {
        EMAIL,
        SMS
    }
    
    // Constructors
    public Notification() {
    }
    
    public Notification(User user, NotificationType type, NotificationChannel channel,
                       String recipient, String subject, String message) {
        this.user = user;
        this.type = type;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.sentAt = LocalDateTime.now();
        this.sent = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public NotificationChannel getChannel() {
        return channel;
    }
    
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isSent() {
        return sent;
    }
    
    public void setSent(boolean sent) {
        this.sent = sent;
    }
}

package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.model.User;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;
import es.codeurjc.service.notifications.NotificationService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class AccountNotificationService {

    private static final String DEPOSIT_CONFIRMATION_MESSAGE = "Deposit Confirmation";
    private static final String WITHDRAWAL_CONFIRMATION_MESSAGE = "Withdrawal Confirmation";
    private static final String WITHDRAWAL_MESSAGE = "Withdrawal";
    private static final String TRANSFER_SENT_MESSAGE = "Transfer Sent";
    private static final String TRANSFER_RECEIVED_MESSAGE = "Transfer Received";

    private final Map<User.NotificationType, BaseNotificationStrategy> strategies = new EnumMap<>(User.NotificationType.class);

    public AccountNotificationService(EmailNotificationService emailService, SmsNotificationService smsService) {
        strategies.put(User.NotificationType.EMAIL, new EmailNotificationStrategy(emailService));
        strategies.put(User.NotificationType.SMS, new SmsNotificationStrategy(smsService));
    }

    public void notifyDeposit(Account account, double amount) {
        BaseNotificationStrategy strategy = getStrategy(account.getUser());
        if (strategy != null) {
            strategy.notifyDeposit(account, amount);
        }
    }

    public void notifyWithdrawal(Account account, double amount) {
        BaseNotificationStrategy strategy = getStrategy(account.getUser());
        if (strategy != null) {
            strategy.notifyWithdrawal(account, amount);
        }
    }

    public void notifyTransferSent(Account account, double amount, String toAccountNumber) {
        BaseNotificationStrategy strategy = getStrategy(account.getUser());
        if (strategy != null) {
            strategy.notifyTransferSent(account, amount, toAccountNumber);
        }
    }

    public void notifyTransferReceived(Account account, double amount, String fromAccountNumber) {
        BaseNotificationStrategy strategy = getStrategy(account.getUser());
        if (strategy != null) {
            strategy.notifyTransferReceived(account, amount, fromAccountNumber);
        }
    }

    private BaseNotificationStrategy getStrategy(User user) {
        User.NotificationType type = user.getNotificationType();
        return type != null ? strategies.get(type) : null;
    }

    private abstract class BaseNotificationStrategy {

        protected final NotificationService notificationService;

        public BaseNotificationStrategy(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        public abstract void notifyDeposit(Account account, double amount);

        public abstract void notifyWithdrawal(Account account, double amount);

        public void notifyTransferSent(Account account, double amount, String toAccountNumber) {
            String content = String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR",
                    amount, toAccountNumber, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_SENT_MESSAGE, content);
        }

        public void notifyTransferReceived(Account account, double amount, String fromAccountNumber) {
            String content = String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                    amount, fromAccountNumber, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_RECEIVED_MESSAGE, content);
        }
    }

    private class EmailNotificationStrategy extends BaseNotificationStrategy {

        public EmailNotificationStrategy(EmailNotificationService service) {
            super(service);
        }

        @Override
        public void notifyDeposit(Account account, double amount) {
            String content = String.format("Deposit of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.DEPOSIT, DEPOSIT_CONFIRMATION_MESSAGE, content);
        }

        @Override
        public void notifyWithdrawal(Account account, double amount) {
            String content = String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.WITHDRAWAL, WITHDRAWAL_CONFIRMATION_MESSAGE, content);
        }
    }

    private class SmsNotificationStrategy extends BaseNotificationStrategy {

        public SmsNotificationStrategy(SmsNotificationService service) {
            super(service);
        }

        @Override
        public void notifyDeposit(Account account, double amount) {
            String content = String.format("Deposit: %.2f EUR. Balance: %.2f EUR", amount, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.DEPOSIT, DEPOSIT_CONFIRMATION_MESSAGE, content);
        }

        @Override
        public void notifyWithdrawal(Account account, double amount) {
            String content = String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance());
            notificationService.sendNotification(account.getUser(), Notification.NotificationType.WITHDRAWAL, WITHDRAWAL_MESSAGE, content);
        }
    }
}

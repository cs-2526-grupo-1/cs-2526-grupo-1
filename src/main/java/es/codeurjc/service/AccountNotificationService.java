package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.model.User;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;
import org.springframework.stereotype.Service;

@Service
public class AccountNotificationService {

    private static final String DEPOSIT_CONFIRMATION_MESSAGE = "Deposit Confirmation";
    private static final String WITHDRAWAL_CONFIRMATION_MESSAGE = "Withdrawal Confirmation";
    private static final String WITHDRAWAL_MESSAGE = "Withdrawal";
    private static final String TRANSFER_SENT_MESSAGE = "Transfer Sent";
    private static final String TRANSFER_RECEIVED_MESSAGE = "Transfer Received";

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public AccountNotificationService(EmailNotificationService emailService, SmsNotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public void notifyDeposit(Account account, double amount) {
        User user = account.getUser();
        String content;

        if (user.getNotificationType() == User.NotificationType.SMS) {
            content = String.format("Deposit: %.2f EUR. Balance: %.2f EUR", amount, account.getBalance());
        } else {
            content = String.format("Deposit of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance());
        }

        send(user, Notification.NotificationType.DEPOSIT, DEPOSIT_CONFIRMATION_MESSAGE, content);
    }

    public void notifyWithdrawal(Account account, double amount) {
        User user = account.getUser();
        String content = String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance());

        String subject = (user.getNotificationType() == User.NotificationType.SMS)
                ? WITHDRAWAL_MESSAGE
                : WITHDRAWAL_CONFIRMATION_MESSAGE;

        send(user, Notification.NotificationType.WITHDRAWAL, subject, content);
    }

    public void notifyTransferSent(Account account, double amount, String toAccountNumber) {
        String content = String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR",
                amount, toAccountNumber, account.getBalance());
        send(account.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_SENT_MESSAGE, content);
    }

    public void notifyTransferReceived(Account account, double amount, String fromAccountNumber) {
        String content = String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                amount, fromAccountNumber, account.getBalance());
        send(account.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_RECEIVED_MESSAGE, content);
    }

    private void send(User user, Notification.NotificationType type, String subject, String content) {
        User.NotificationType pref = user.getNotificationType();
        if (pref == User.NotificationType.EMAIL) {
            emailService.sendNotification(user, type, subject, content);
        } else if (pref == User.NotificationType.SMS) {
            smsService.sendNotification(user, type, subject, content);
        }
    }
}
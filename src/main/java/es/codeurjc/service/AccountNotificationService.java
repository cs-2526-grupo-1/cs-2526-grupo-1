package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class AccountNotificationService {

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

    //aux method that returns the desired strategy given the users notifications preference
    private BaseNotificationStrategy getStrategy(User user) {
        User.NotificationType preference = user.getNotificationType();
        return preference != null ? strategies.get(preference) : null;
    }
}

package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.service.notifications.SmsNotificationService;

class SmsNotificationStrategy extends BaseNotificationStrategy {

    private static final String WITHDRAWAL_MESSAGE = "Withdrawal";

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

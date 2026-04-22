package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.service.notifications.NotificationService;

//Strategy class from which the concrete Strategies are implemented
abstract class BaseNotificationStrategy {

    protected static final String DEPOSIT_CONFIRMATION_MESSAGE = "Deposit Confirmation";
    protected static final String TRANSFER_SENT_MESSAGE = "Transfer Sent";
    protected static final String TRANSFER_RECEIVED_MESSAGE = "Transfer Received";

    protected final NotificationService notificationService;

    public BaseNotificationStrategy(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //abstract method that will be implemented by concrete strategies
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

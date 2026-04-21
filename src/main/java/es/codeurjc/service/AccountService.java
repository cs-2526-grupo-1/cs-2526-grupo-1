package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.model.Notification;
import es.codeurjc.model.Transaction;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing bank accounts.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final RandomService randomService;

    private static final double MAX_DEPOSIT_LIMIT = 10000;
    private static final double MAX_WITHDRAWAL_LIMIT = 5000;
    private static final double MAX_TRANSFER_LIMIT = 20000;

    public AccountService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            EmailNotificationService emailService,
            SmsNotificationService smsService,
            RandomService randomService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.randomService = randomService;
    }

    /**
     * Create a new account
     */
    public Account createAccount(User user, Account.AccountType accountType) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountType, 0);
        account.setUser(user);
        return accountRepository.save(account);
    }

    /**
     * Generate account number
     */
    private String generateAccountNumber() {
        return String.format("ES%010d", randomService.nextInt(1000000000));
    }

    /**
     * Get account by account number
     */
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    /**
     * Get all accounts for a user
     */
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    /**
     * Deposit money into account
     */
    @Transactional
    public Account deposit(String accountNumber, double amount, String description) {
        validateAmount(amount, MAX_DEPOSIT_LIMIT, "Amount exceeds maximum deposit limit");

        Account account = getAccount(accountNumber);
        account.deposit(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        sendNotification(account, Notification.NotificationType.DEPOSIT,
                "Deposit Confirmation",
                String.format("Deposit of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()),
                "Deposit Confirmation",
                String.format("Deposit: %.2f EUR. Balance: %.2f EUR", amount, account.getBalance()));

        return savedAccount;
    }

    /**
     * Quick deposit without description
     */
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        validateAmount(amount, MAX_DEPOSIT_LIMIT, "Amount exceeds maximum deposit limit");

        Account account = getAccount(accountNumber);
        account.deposit(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount, "Quick deposit");
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        sendNotification(account, Notification.NotificationType.DEPOSIT,
                "Deposit Confirmation",
                String.format("Deposit of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()),
                "Deposit Confirmation",
                String.format("Deposit: %.2f EUR. Balance: %.2f EUR", amount, account.getBalance()));

        return savedAccount;
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(String accountNumber, double amount, String description) {
        validateAmount(amount, MAX_WITHDRAWAL_LIMIT, "Amount exceeds maximum withdrawal limit");

        Account account = getAccount(accountNumber);
        validateSufficientFunds(account, amount);

        account.withdraw(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL,
                amount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        sendNotification(account, Notification.NotificationType.WITHDRAWAL,
                "Withdrawal Confirmation",
                String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()),
                "Withdrawal",
                String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()));

        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        validateAmount(amount, MAX_TRANSFER_LIMIT, "Amount exceeds maximum transfer limit");

        Account m = getAccount(fromAccountNumber);
        Account o = getAccount(toAccountNumber);

        // Validate same account
        if (m.getAccountNumber().equals(o.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        validateSufficientFunds(m, amount);

        // Perform transfer
        m.withdraw(amount);
        o.deposit(amount);

        // Record transactions
        Transaction sentTransaction = new Transaction(m,
                Transaction.TransactionType.TRANSFER_SENT,
                amount,
                "Transfer to " + toAccountNumber);
        sentTransaction.setDestinationAccountNumber(toAccountNumber);
        transactionRepository.save(sentTransaction);

        Transaction receivedTransaction = new Transaction(o,
                Transaction.TransactionType.TRANSFER_RECEIVED,
                amount,
                "Transfer from " + fromAccountNumber);
        receivedTransaction.setDestinationAccountNumber(fromAccountNumber);
        transactionRepository.save(receivedTransaction);

        accountRepository.save(m);
        accountRepository.save(o);

        sendNotification(m, Notification.NotificationType.TRANSFER, "Transfer Sent",
                String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR", amount, toAccountNumber,
                        m.getBalance()));

        sendNotification(o, Notification.NotificationType.TRANSFER, "Transfer Received",
                String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR", amount, fromAccountNumber,
                        o.getBalance()));
    }

    /**
     * Delete account
     */
    public void rm(String accountNumber) {
        Account account = getAccount(accountNumber);

        if (account.getBalance() != 0) {
            throw new IllegalArgumentException("Cannot delete account with non-zero balance");
        }

        accountRepository.delete(account);
    }

    /**
     * Get account balance
     */
    public double getBalance(String accountNumber) {
        Account account = getAccount(accountNumber);
        return account.getBalance();
    }

    /**
     * Get account transactions
     */
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    private void validateAmount(double amount, double limit, String limitExceededMessage) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > limit) {
            throw new IllegalArgumentException(limitExceededMessage);
        }
    }

    private void validateSufficientFunds(Account account, double amount) {
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

    private void sendNotification(Account account, Notification.NotificationType type, String subject, String message) {
        sendNotification(account, type, subject, message, subject, message);
    }

    private void sendNotification(Account account, Notification.NotificationType type,
            String emailSubject, String emailMessage, String smsSubject, String smsMessage) {
        User user = account.getUser();
        User.NotificationType notifType = user.getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(user, type, emailSubject, emailMessage);
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(user, type, smsSubject, smsMessage);
        }
    }
}

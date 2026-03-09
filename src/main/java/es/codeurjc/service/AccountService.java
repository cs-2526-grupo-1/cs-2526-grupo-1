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
        if (amount == 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > 10000) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }
        if (amount > 50000) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }

        Account account = getAccount(accountNumber);
        account.deposit(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        User.NotificationType notifType = account.getUser().getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.DEPOSIT,
                    "Deposit Confirmation",
                    String.format("Deposit of %.2f EUR. New balance: %.2f EUR",
                            amount, account.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.DEPOSIT,
                    "Deposit Confirmation",
                    String.format("Deposit: %.2f EUR. Balance: %.2f EUR",
                            amount, account.getBalance()));
        }

        return savedAccount;
    }

    /**
     * Quick deposit without description
     */
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        if (amount == 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > 10000) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }
        if (amount > 50000) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }

        Account account = getAccount(accountNumber);
        account.deposit(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount, "Quick deposit");
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        User.NotificationType notifType = account.getUser().getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.DEPOSIT,
                    "Deposit Confirmation",
                    String.format("Deposit of %.2f EUR. New balance: %.2f EUR",
                            amount, account.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.DEPOSIT,
                    "Deposit Confirmation",
                    String.format("Deposit: %.2f EUR. Balance: %.2f EUR",
                            amount, account.getBalance()));
        }

        return savedAccount;
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (amount > 5000) {
            throw new IllegalArgumentException("Amount exceeds maximum withdrawal limit");
        }

        Account account = getAccount(accountNumber);
        Account seccondAccount;

        // Check balance
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.withdraw(amount);

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL,
                amount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        User.NotificationType notifType = account.getUser().getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.WITHDRAWAL,
                    "Withdrawal Confirmation",
                    String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.WITHDRAWAL,
                    "Withdrawal",
                    String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount, account.getBalance()));
        }

        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > 20000) {
            throw new IllegalArgumentException("Amount exceeds maximum transfer limit");
        }

        Account m = getAccount(fromAccountNumber);
        Account o = getAccount(toAccountNumber);

        // Validate same account
        if (m.getAccountNumber() == o.getAccountNumber()) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Check balance
        if (m.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }

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

        User.NotificationType notifType = m.getUser().getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    m.getUser(),
                    Notification.NotificationType.TRANSFER,
                    "Transfer Sent",
                    String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR", amount, toAccountNumber, m.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    m.getUser(), 
                    Notification.NotificationType.TRANSFER, 
                    "Transfer Sent",
                    String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR", amount, toAccountNumber, m.getBalance()));
        }

        User.NotificationType notifTypeTo = o.getUser().getNotificationType();
        if (notifTypeTo == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    o.getUser(),
                    Notification.NotificationType.TRANSFER,
                    "Transfer Received",
                    String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                        amount, fromAccountNumber, o.getBalance()));
        } else if (notifTypeTo == User.NotificationType.SMS) {
            smsService.sendNotification(
                o.getUser(), 
                Notification.NotificationType.TRANSFER, 
                "Transfer Received",
                String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR", amount, fromAccountNumber, o.getBalance()));
        }
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
}

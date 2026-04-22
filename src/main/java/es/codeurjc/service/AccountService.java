package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Amount;
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

    private static final String DEPOSIT_CONFIRMATION_MESSAGE = "Deposit Confirmation";
    private static final String WITHDRAWAL_CONFIRMATION_MESSAGE = "Withdrawal Confirmation";
    private static final String WITHDRAWAL_MESSAGE = "Withdrawal";
    private static final String TRANSFER_SENT_MESSAGE = "Transfer Sent";
    private static final String TRANSFER_RECEIVED_MESSAGE = "Transfer Received";

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
        String accountNumber;

        do {
            accountNumber = String.format("ES%010d", randomService.nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
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
    public Account deposit(String accountNumber, Amount amount, String description) {
        
        validateAmount(amount, Amount.fromDouble(10000), "Amount exceeds maximum deposit limit");

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
                    DEPOSIT_CONFIRMATION_MESSAGE,
                    String.format("Deposit of %.2f EUR. New balance: %.2f EUR",
                            amount.getValue(), account.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.DEPOSIT,
                    DEPOSIT_CONFIRMATION_MESSAGE,
                    String.format("Deposit: %.2f EUR. Balance: %.2f EUR",
                            amount.getValue(), account.getBalance()));
        }

        return savedAccount;
    }

    /**
     * Quick deposit without description
     */
    @Transactional
    public Account deposit(String accountNumber, Amount amount) {
        return this.deposit(accountNumber, amount, "Quick deposit");
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(String accountNumber, Amount amount, String description) {
        validateAmount(amount, Amount.fromDouble(5000), "Amount exceeds maximum withdrawal limit");

        Account account = getAccount(accountNumber);
        Account seccondAccount;

        // Check balance
        if (account.getBalance() < amount.getValue().doubleValue()) {
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
                    WITHDRAWAL_CONFIRMATION_MESSAGE,
                    String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount.getValue(), account.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    account.getUser(),
                    Notification.NotificationType.WITHDRAWAL,
                    WITHDRAWAL_MESSAGE,
                    String.format("Withdrawal of %.2f EUR. New balance: %.2f EUR", amount.getValue(), account.getBalance()));
        }

        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, Amount amount) {
        validateAmount(amount, Amount.fromDouble(20000), "Amount exceeds maximum transfer limit");

        Account sourceAccount = getAccount(fromAccountNumber);
        Account destinationAccount = getAccount(toAccountNumber);

        // Validate same account
        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Check balance
        if (sourceAccount.getBalance() < amount.getValue().doubleValue()) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Perform transfer
        sourceAccount.withdraw(amount);
        destinationAccount.deposit(amount);

        // Record transactions
        Transaction sentTransaction = new Transaction(sourceAccount,
                Transaction.TransactionType.TRANSFER_SENT,
                amount,
                "Transfer to " + toAccountNumber);
        sentTransaction.setDestinationAccountNumber(toAccountNumber);
        transactionRepository.save(sentTransaction);

        Transaction receivedTransaction = new Transaction(destinationAccount,
                Transaction.TransactionType.TRANSFER_RECEIVED,
                amount,
                "Transfer from " + fromAccountNumber);
        receivedTransaction.setDestinationAccountNumber(fromAccountNumber);
        transactionRepository.save(receivedTransaction);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        User.NotificationType notifType = sourceAccount.getUser().getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    sourceAccount.getUser(),
                    Notification.NotificationType.TRANSFER,
                    TRANSFER_SENT_MESSAGE,
                    String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR", amount.getValue(), toAccountNumber, sourceAccount.getBalance()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    sourceAccount.getUser(), 
                    Notification.NotificationType.TRANSFER, 
                    TRANSFER_SENT_MESSAGE,
                    String.format("Transfer of %.2f EUR to %s. New balance: %.2f EUR", amount.getValue(), toAccountNumber, sourceAccount.getBalance()));
        }

        User.NotificationType notifTypeTo = destinationAccount.getUser().getNotificationType();
        if (notifTypeTo == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    destinationAccount.getUser(),
                    Notification.NotificationType.TRANSFER,
                    TRANSFER_RECEIVED_MESSAGE,
                    String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                        amount.getValue(), fromAccountNumber, destinationAccount.getBalance()));
        } else if (notifTypeTo == User.NotificationType.SMS) {
            smsService.sendNotification(
                destinationAccount.getUser(), 
                Notification.NotificationType.TRANSFER, 
                TRANSFER_RECEIVED_MESSAGE,
                String.format("Transfer of %.2f EUR from %s. New balance: %.2f EUR", amount.getValue(), fromAccountNumber, destinationAccount.getBalance()));
        }
    }

    /**
     * Delete account
     */
    public void removeAccount(String accountNumber) {
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

    private void validateAmount(Amount amount, Amount max, String errorMsg) {
        if (!(amount.isPositive())) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.isGreaterThan(max)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

}

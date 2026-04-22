package es.codeurjc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.notifications.NotificationService;

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
    //private final List<NotificationService> notificationServices;
    private final RandomService randomService;

    private final Map<String, NotificationService> notificationServices;

    public AccountService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            List<NotificationService> services,
            RandomService randomService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationServices = services.stream().collect(Collectors.toMap(s -> s.getChannel().name(), s -> s));
        this.randomService = randomService;
    }

    /**
     *
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
     * Send notification to user
     */
    private void sendNotification(User user, Notification.NotificationType type, String subject, String message) {

        if (user.getNotificationType() == null) {
            return;
        }

        //user's preferred notification type is extracted. Notification is sent through the corresponding channel applying polymorphism
        NotificationService service = notificationServices.get(user.getNotificationType().name());

        if (service != null) {
            service.sendNotification(user, type, subject, message);
        }

    }

    /**
     * Deposit money into account
     */
    @Transactional
    public Account deposit(String accountNumber, double amount, String description) {
        double roundedAmount = round(amount);
        validateAmount(roundedAmount, 10000.0, "Amount exceeds maximum deposit limit");

        Account account = getAccount(accountNumber);

        account.deposit(roundedAmount);

        account.setBalance(round(account.getBalance())); //as it is still a double the result still has the same problem->we have to fix it as above

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                roundedAmount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        sendNotification(account.getUser(), Notification.NotificationType.DEPOSIT, DEPOSIT_CONFIRMATION_MESSAGE,
                String.format("Deposit of %.2f EUR. Balance: %.2f EUR", roundedAmount, account.getBalance()));

        return savedAccount;
    }

    /**
     * Quick deposit without description
     */
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        return this.deposit(accountNumber, amount, "Quick deposit");
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(String accountNumber, double amount, String description) {
        double roundedAmount = round(amount);
        validateAmount(roundedAmount, 5000.0, "Amount exceeds maximum withdrawal limit");

        Account account = getAccount(accountNumber);

        // Check balance
        if (BigDecimal.valueOf(account.getBalance()).compareTo(BigDecimal.valueOf(roundedAmount)) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.withdraw(roundedAmount);
        account.setBalance(round(account.getBalance())); // the same as above

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL,
                roundedAmount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        sendNotification(account.getUser(), Notification.NotificationType.WITHDRAWAL, WITHDRAWAL_CONFIRMATION_MESSAGE,
                String.format("Withdrawal of %.2f EUR. Balance: %.2f EUR", roundedAmount, account.getBalance()));

        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        double roundedAmount = round(amount);
        validateAmount(roundedAmount, 20000.0, "Amount exceeds maximum transfer limit");

        Account sourceAccount = getAccount(fromAccountNumber);
        Account destinationAccount = getAccount(toAccountNumber);

        // Validate same account
        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Check balance
        if (BigDecimal.valueOf(sourceAccount.getBalance()).compareTo(BigDecimal.valueOf(roundedAmount)) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Perform transfer
        sourceAccount.withdraw(roundedAmount);
        sourceAccount.setBalance(round(sourceAccount.getBalance()));

        destinationAccount.deposit(roundedAmount);
        destinationAccount.setBalance(round(destinationAccount.getBalance()));

        // Record transactions
        Transaction sentTransaction = new Transaction(sourceAccount,
                Transaction.TransactionType.TRANSFER_SENT,
                roundedAmount,
                "Transfer to " + toAccountNumber);
        sentTransaction.setDestinationAccountNumber(toAccountNumber);
        transactionRepository.save(sentTransaction);

        Transaction receivedTransaction = new Transaction(destinationAccount,
                Transaction.TransactionType.TRANSFER_RECEIVED,
                roundedAmount,
                "Transfer from " + fromAccountNumber);
        receivedTransaction.setDestinationAccountNumber(fromAccountNumber);
        transactionRepository.save(receivedTransaction);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        sendNotification(sourceAccount.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_SENT_MESSAGE,
                String.format("Transfer of %.2f EUR to %s. Balance: %.2f EUR", roundedAmount, toAccountNumber, sourceAccount.getBalance()));

        sendNotification(destinationAccount.getUser(), Notification.NotificationType.TRANSFER, TRANSFER_RECEIVED_MESSAGE,
                String.format("Transfer of %.2f EUR from %s. Balance: %.2f EUR", roundedAmount, fromAccountNumber, destinationAccount.getBalance()));

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
        return round(getAccount(accountNumber).getBalance());
    }

    /**
     * Get account transactions
     */
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    private void validateAmount(double amount, double max, String errorMsg) {
        BigDecimal bdAmount = BigDecimal.valueOf(amount);

        if (bdAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (bdAmount.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
}

package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.model.Transaction;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for managing bank accounts.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNotificationService notificationService;
    private final AccountValidationService validationService;
    private final RandomService randomService;

    public AccountService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AccountNotificationService notificationService,
            AccountValidationService validationService,
            RandomService randomService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
        this.validationService = validationService;
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
     * Generate account number. This method is an infinite loop with 1000000000
     * ACCOUNTS, we assume the app scope is not that big.
     * In case of needing to fix this we could change with ids for example. But this
     * changes the business logic, so it's not asked
     * for the purpose of this task.
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
    public Account deposit(String accountNumber, double amount, String description) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 10000.0, "Amount exceeds maximum deposit limit");

        Account account = getAccount(accountNumber);
        account.deposit(roundedAmount);
        account.setBalance(round(account.getBalance()));

        recordTransaction(account, Transaction.TransactionType.DEPOSIT, roundedAmount, description);
        Account savedAccount = accountRepository.save(account);
        notificationService.notifyDeposit(account, roundedAmount);
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
        validationService.validateAmount(roundedAmount, 5000.0, "Amount exceeds maximum withdrawal limit");

        Account account = getAccount(accountNumber);
        validationService.checkSufficientFunds(roundedAmount, account.getBalance());

        account.withdraw(roundedAmount);
        account.setBalance(round(account.getBalance()));

        recordTransaction(account, Transaction.TransactionType.WITHDRAWAL, roundedAmount, description);
        Account savedAccount = accountRepository.save(account);
        notificationService.notifyWithdrawal(account, roundedAmount);
        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 20000.0, "Amount exceeds maximum transfer limit");

        Account sourceAccount = getAccount(fromAccountNumber);
        Account destinationAccount = getAccount(toAccountNumber);

        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Check balance in source account
        validationService.checkSufficientFunds(roundedAmount, sourceAccount.getBalance());

        // Withdraw from source and deposit to destination
        withdrawForTransferAndSave(sourceAccount, roundedAmount, toAccountNumber);
        depositFromTransferAndSave(destinationAccount, roundedAmount, fromAccountNumber);

        // Send notifications to both users
        notificationService.notifyTransferSent(sourceAccount, roundedAmount, toAccountNumber);
        notificationService.notifyTransferReceived(destinationAccount, roundedAmount, fromAccountNumber);
    }

    /**
     * Delete account
     */
    public void removeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);

        validationService.validateAccountDeletion(account);

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

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    private void recordTransaction(Account account, Transaction.TransactionType type,
            double amount, String description) {
        recordTransaction(account, type, amount, description, null);
    }

    private void recordTransaction(Account account, Transaction.TransactionType type,
            double amount, String description, String relatedAccountNumber) {
        Transaction transaction = new Transaction(account, type, amount, description);
        if (relatedAccountNumber != null) {
            transaction.setDestinationAccountNumber(relatedAccountNumber);
        }
        transactionRepository.save(transaction);
    }

    private void withdrawForTransferAndSave(Account account, double amount, String toAccountNumber) {
        account.withdraw(amount);
        account.setBalance(round(account.getBalance()));
        recordTransaction(account, Transaction.TransactionType.TRANSFER_SENT,
                amount, "Transfer to " + toAccountNumber, toAccountNumber);
        accountRepository.save(account);
    }

    private void depositFromTransferAndSave(Account account, double amount, String fromAccountNumber) {
        account.deposit(amount);
        account.setBalance(round(account.getBalance()));
        recordTransaction(account, Transaction.TransactionType.TRANSFER_RECEIVED,
                amount, "Transfer from " + fromAccountNumber, fromAccountNumber);
        accountRepository.save(account);
    }
}
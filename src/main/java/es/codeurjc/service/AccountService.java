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

// Service for managing accounts, including creation, retrieval, deposits, withdrawals, transfers, and deletion
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

    // Method to create a new account for a user with a specified account type
    // It generates a unique account number and initializes the balance to zero
    public Account createAccount(User user, Account.AccountType accountType) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountType, 0);
        account.setUser(user);
        return accountRepository.save(account);
    }

    // Method to generate a unique account number in the format "ES" followed by 10 digits
    // It ensures that the generated account number does not already exist in the repository to avoid duplicates
    private String generateAccountNumber() {
        String accountNumber;

        do {
            accountNumber = String.format("ES%010d", randomService.nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // Get account by account number, throwing an exception if the account is not found
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    // Method to get all acounts for a specified user
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    // Method to deposit money into an account
    // It validates the amount, updates the account balance, records the transaction, and sends a notification to the user
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

    // Overloaded method to deposit money with a default description, 
    // for convenience in tests and other cases where a custom description is not needed
    // It calls the main deposit method with a default description of "Quick deposit"
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        return this.deposit(accountNumber, amount, "Quick deposit");
    }

    // Method to withdraw money from an account
    // It validates the amount, checks for sufficient funds, updates the account balance, 
    // records the transaction, and sends a notification to the user
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

    // Method to transfer money between accounts
    // It validates the amount, checks for sufficient funds in the source account, updates the accounts balances, 
    // records the transactions, and sends notifications to the users of both accounts
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 20000.0, "Amount exceeds maximum transfer limit");

        Account sourceAccount = getAccount(fromAccountNumber);
        Account destinationAccount = getAccount(toAccountNumber);

        validationService.validateUserAgeForTransfer(sourceAccount.getUser());

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

    // Method to remove an account by its account number
    // It retrieves the account, validates that it can be deleted
    // and then deletes it from the repository
    public void removeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);

        validationService.validateAccountDeletion(account);

        accountRepository.delete(account);
    }

    // Method to get the current balance of an account by its account number
    public double getBalance(String accountNumber) {
        return round(getAccount(accountNumber).getBalance());
    }

    // Method to get the transactions of an account by its account number
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    // Method to round a double value to 2 decimal places using HALF_EVEN rounding mode,
    // which is commonly used for financial calculations to minimize rounding errors
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    // Overloaded method to record a transaction with a null description,
    // for convenience in cases where a custom description is not needed
    private void recordTransaction(Account account, Transaction.TransactionType type,
            double amount, String description) {
        recordTransaction(account, type, amount, description, null);
    }

    // Method to record a transaction in the repository, with an optional related account number for transfers
    private void recordTransaction(Account account, Transaction.TransactionType type,
            double amount, String description, String relatedAccountNumber) {
        Transaction transaction = new Transaction(account, type, amount, description);
        if (relatedAccountNumber != null) {
            transaction.setDestinationAccountNumber(relatedAccountNumber);
        }
        transactionRepository.save(transaction);
    }

    // Method to perform the withdrawal part of a transfer and save the updated account,
    // it also records the transaction with a description indicating the transfer and the destination account number
    private void withdrawForTransferAndSave(Account account, double amount, String toAccountNumber) {
        account.withdraw(amount);
        account.setBalance(round(account.getBalance()));
        recordTransaction(account, Transaction.TransactionType.TRANSFER_SENT,
                amount, "Transfer to " + toAccountNumber, toAccountNumber);
        accountRepository.save(account);
    }

    // Method to perform the deposit part of a transfer and save the updated account,
    // it also records the transaction with a description indicating the transfer and the source account number
    private void depositFromTransferAndSave(Account account, double amount, String fromAccountNumber) {
        account.deposit(amount);
        account.setBalance(round(account.getBalance()));
        recordTransaction(account, Transaction.TransactionType.TRANSFER_RECEIVED,
                amount, "Transfer from " + fromAccountNumber, fromAccountNumber);
        accountRepository.save(account);
    }
}

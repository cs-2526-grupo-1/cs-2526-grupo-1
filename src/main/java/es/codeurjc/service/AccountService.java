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

// This service contains the business logic related to accounts, such as creating accounts, performing transactions, etc
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

    // Method to create a new account for a user. It generates a unique account number, assigns the user to the account, and saves the account in the repository
    public Account createAccount(User user, Account.AccountType accountType) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountType, 0);
        account.setUser(user);
        return accountRepository.save(account);
    }

    // Method to create a random account number, ensuring it is not duplicated
    private String generateAccountNumber() {
        String accountNumber;

        do {
            accountNumber = String.format("ES%010d", randomService.nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    
    // Method to get an account by its account number. If the account does not exist, it throws an exception
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    // Method to get all accounts of a user
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    // Method to perform a deposit into an account. 
    // It validates the amount, updates the account balance, records the transaction, 
    // and sends a notification using the notification service. The method is transactional to ensure data consistency.
    @Transactional
    public Account deposit(String accountNumber, double amount, String description) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 10000.0, "Amount exceeds maximum deposit limit");

        Account account = getAccount(accountNumber);

        account.deposit(roundedAmount);

        account.setBalance(round(account.getBalance())); //as it is still a double the result still has the same problem->we have to fix it as above

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                roundedAmount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        notificationService.notifyDeposit(account, roundedAmount);

        return savedAccount;
    }

   // Method to perform a deposit with a default description
   // It calls the previous method with a default description of "Quick deposit" 
   // The method is transactional to ensure data consistency
    @Transactional
    public Account deposit(String accountNumber, double amount) {
        return this.deposit(accountNumber, amount, "Quick deposit");
    }

    // Method to withdraw money from an account    
    // It validates the amount, checks for sufficient funds, updates the account balance, records the transaction, 
    // and sends a notification using the notification service. The method is transactional to ensure data consistency
    @Transactional
    public Account withdraw(String accountNumber, double amount, String description) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 5000.0, "Amount exceeds maximum withdrawal limit");

        Account account = getAccount(accountNumber);

        // Check balance
        validationService.checkSufficientFunds(roundedAmount, account.getBalance());

        account.withdraw(roundedAmount);
        account.setBalance(round(account.getBalance())); // the same as above

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL,
                roundedAmount, description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        notificationService.notifyWithdrawal(account, roundedAmount);

        return savedAccount;
    }

    // Method to transfer money from one account to another. 
    // It validates the amount, checks for sufficient funds in source account, 
    // updates the balances of both accounts, records the transactions for both accounts, 
    // and sends notifications to both account holders using the notification service. 
    // The method is transactional to ensure data consistency
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        double roundedAmount = round(amount);
        validationService.validateAmount(roundedAmount, 20000.0, "Amount exceeds maximum transfer limit");

        Account sourceAccount = getAccount(fromAccountNumber);
        Account destinationAccount = getAccount(toAccountNumber);

        // Validate same account
        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Check balance
        validationService.checkSufficientFunds(roundedAmount, sourceAccount.getBalance());

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

        notificationService.notifyTransferSent(sourceAccount, roundedAmount, toAccountNumber);
        notificationService.notifyTransferReceived(destinationAccount, roundedAmount, fromAccountNumber);
    }

    // Delete an account by its account number 
    // It validates that the account can be deleted and then deletes it from the repository
    public void removeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);

        validationService.validateAccountDeletion(account);

        accountRepository.delete(account);
    }

    // Method to get the balance of an account by its account number
    // It retrieves the account and returns its balance rounded to 2 decimals
    public double getBalance(String accountNumber) {
        return round(getAccount(accountNumber).getBalance());
    }

    // Method to get the transaction history of an account by its account number
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    // Helper method to round a double value to 2 decimal places using BigDecimal for accurate rounding
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
}
package es.codeurjc.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Account entity representing a bank account.
 */
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    private BigDecimal balance;
    
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();
    
    public enum AccountType {
        CHECKING,
        SAVINGS,
        DEPOSIT
    }
    
    // Constructors
    public Account() {
    }
    
    public Account(String accountNumber, AccountType accountType, double balance) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_EVEN);
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public double getBalance() {
        return balance.doubleValue();
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    /**
     * Deposit money into the account
     * @param amount amount to deposit
     */
    public void deposit(Amount amount) {
        if (!(amount.isPositive())) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        setBalance(balance.add(amount.getValue()));
    }
    
    /**
     * Withdraw money from the account
     * @param amount amount to withdraw
     */
    public void withdraw(Amount amount) {
        if (!(amount.isPositive())) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.isGreaterThan(new Amount(BigDecimal.valueOf(getBalance())))) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        setBalance(balance.subtract(amount.getValue()));
    }
    
}

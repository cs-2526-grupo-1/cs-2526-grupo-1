package es.codeurjc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a bank transaction.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    private double amount;
    
    private String description;
    private LocalDateTime timestamp;
    
    // Para transferencias
    private String destinationAccountNumber;
    
    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_SENT,
        TRANSFER_RECEIVED,
        LOAN_DISBURSEMENT,
        LOAN_PAYMENT
    }
    
    // Constructors
    public Transaction() {
    }
    
    public Transaction(Account account, TransactionType type, double amount, String description) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }
    
    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }
}

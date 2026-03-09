package es.codeurjc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Loan entity representing a customer loan.
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private double requestedAmount;
    private double approvedAmount;
    private double remainingAmount;

    private int termMonths; // plazo en meses
    private double interestRate; // tasa de interés anual
    private double monthlyPayment;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private String rejectionReason;

    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;

    private int totalPayments;
    private int paidPayments;
    private int latePayments; // pagos retrasados

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "evaluation_result_id")
    private LoanEvaluationResult evaluationResult;

    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED,
        PAID;

        @Override
        public String toString() {
            return this.name();
        }
    }

    // Constructors
    public Loan() {
    }

    public Loan(User user, double requestedAmount, int termMonths) {
        this.user = user;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.status = LoanStatus.PENDING;
        this.requestDate = LocalDateTime.now();
        this.paidPayments = 0;
        this.latePayments = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(double approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public LoanStatus getStatus() {
        return status;
    }

    // Helper method for Mustache templates
    public String getStatusName() {
        return status != null ? status.name() : "UNKNOWN";
    }

    // Alternative getter that Mustache might prefer
    public String getStatusText() {
        return getStatusName();
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public int getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(int totalPayments) {
        this.totalPayments = totalPayments;
    }

    public int getPaidPayments() {
        return paidPayments;
    }

    public void setPaidPayments(int paidPayments) {
        this.paidPayments = paidPayments;
    }

    public int getLatePayments() {
        return latePayments;
    }

    public void setLatePayments(int latePayments) {
        this.latePayments = latePayments;
    }

    public LoanEvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public void setEvaluationResult(LoanEvaluationResult evaluationResult) {
        this.evaluationResult = evaluationResult;
    }

    /**
     * Calculate delinquency rate (morosidad)
     * 
     * @return percentage of late payments
     */
    public double getDelinquencyRate() {
        if (paidPayments == 0) {
            return 0.0;
        }
        return (double) latePayments / paidPayments * 100;
    }
}

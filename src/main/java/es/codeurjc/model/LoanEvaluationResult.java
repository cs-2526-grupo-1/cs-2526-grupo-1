package es.codeurjc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_evaluation_results")
public class LoanEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean approved; // true = approved, false = rejected
    private String reason; // Reason for decision
    private double approvedAmount; // Approved amount
    private double interestRate; // Applied interest rate
    private double monthlyPayment; // Monthly payment

    public LoanEvaluationResult() {
    }

    public LoanEvaluationResult(boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason;
    }

    public LoanEvaluationResult(boolean approved, String reason, double approvedAmount, double interestRate,
            double monthlyPayment) {
        this.approved = approved;
        this.reason = reason;
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.monthlyPayment = monthlyPayment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(double approvedAmount) {
        this.approvedAmount = approvedAmount;
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
}

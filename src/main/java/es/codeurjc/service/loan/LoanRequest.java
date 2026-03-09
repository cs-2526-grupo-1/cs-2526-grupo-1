package es.codeurjc.service.loan;

public class LoanRequest {
    // Information about the loan
    private double amount; // Amount requested
    private int termMonths; // Term in months

    // Financial information
    private double customerBalance; // Current balance
    private double monthlyIncome; // Monthly income

    // Customer information
    private String customerDni; // DNI for credit score
    private int yearsWithBank; // Years as customer
    private boolean hasMultipleProducts; // Has multiple products

    public LoanRequest() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public double getCustomerBalance() {
        return customerBalance;
    }

    public void setCustomerBalance(double customerBalance) {
        this.customerBalance = customerBalance;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getCustomerDni() {
        return customerDni;
    }

    public void setCustomerDni(String customerDni) {
        this.customerDni = customerDni;
    }

    public int getYearsWithBank() {
        return yearsWithBank;
    }

    public void setYearsWithBank(int yearsWithBank) {
        this.yearsWithBank = yearsWithBank;
    }

    public boolean isHasMultipleProducts() {
        return hasMultipleProducts;
    }

    public void setHasMultipleProducts(boolean hasMultipleProducts) {
        this.hasMultipleProducts = hasMultipleProducts;
    }
}

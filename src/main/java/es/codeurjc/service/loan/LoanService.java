package es.codeurjc.service.loan;

import es.codeurjc.model.Account;
import es.codeurjc.model.User;
import es.codeurjc.model.Loan;
import es.codeurjc.model.LoanEvaluationResult;
import es.codeurjc.model.Notification;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.LoanRepository;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;
import es.codeurjc.repository.LoanEvaluationResultRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing loans.
 */
@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final LoanApprovalAlgorithm loanApprovalAlgorithm;
    private final LoanEvaluationResultRepository loanEvaluationResultRepository;

    public LoanService(LoanRepository loanRepository,
            AccountRepository accountRepository,
            EmailNotificationService emailService,
            SmsNotificationService smsService,
            LoanEvaluationResultRepository loanEvaluationResultRepository) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.loanApprovalAlgorithm = new LoanApprovalAlgorithm();
        this.loanEvaluationResultRepository = loanEvaluationResultRepository;
    }

    /**
     * Request a loan
     */
    public Loan requestLoan(User user, double amount, int termMonths) {
        Loan loan = new Loan(user, amount, termMonths);

        // Create LoanRequest
        LoanRequest request = new LoanRequest();
        request.setAmount(amount);
        request.setTermMonths(termMonths);
        request.setMonthlyIncome(user.getMonthlyIncome());
        request.setCustomerDni(user.getDni());
        request.setYearsWithBank((int) user.getYearsWithBank());
        request.setHasMultipleProducts(user.hasMultipleProducts());

        // Get account info (assuming first account)
        List<Account> accounts = accountRepository.findByUser(user);
        if (!accounts.isEmpty()) {
            request.setCustomerBalance(accounts.get(0).getBalance());
        }

        // Evaluate
        LoanEvaluationResult result = loanApprovalAlgorithm.evaluate(request);

        // Save result
        loanEvaluationResultRepository.save(result);
        loan.setEvaluationResult(result);

        // Update loan status based on result
        if (!result.isApproved()) {
            loan.setStatus(Loan.LoanStatus.REJECTED);
            loan.setRejectionReason(result.getReason());
        }

        return loanRepository.save(loan);
    }

    /**
     * Approve loan - disburses funds and sends notification
     * The evaluation should already be done by the algorithm in requestLoan
     */
    @Transactional
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalArgumentException("Loan is not pending");
        }

        LoanEvaluationResult result = loan.getEvaluationResult();
        if (result == null || !result.isApproved()) {
            throw new IllegalArgumentException("Loan was not approved by the algorithm");
        }

        User user = loan.getUser();
        List<Account> accounts = accountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            return rejectLoan(loanId, "No account found for user");
        }
        Account account = accounts.get(0);

        // Use values from evaluation result
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovedAmount(result.getApprovedAmount());
        loan.setInterestRate(result.getInterestRate());
        loan.setMonthlyPayment(result.getMonthlyPayment());
        loan.setApprovalDate(LocalDateTime.now());
        loan.setTotalPayments(loan.getTermMonths());
        
        double totalAmount = result.getApprovedAmount() * (1 + result.getInterestRate() / 100);
        loan.setRemainingAmount(totalAmount);

        Loan savedLoan = loanRepository.save(loan);

        // Disburse loan - add money to account
        account.deposit(loan.getApprovedAmount());
        accountRepository.save(account);

        // Send notification
        User.NotificationType notifType = user.getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_APPROVED,
                    "Loan Approved",
                    String.format("Your loan of %.2f EUR has been approved. " +
                            "Monthly payment: %.2f EUR for %d months. " +
                            "Interest rate: %.2f%%",
                            loan.getApprovedAmount(),
                            loan.getMonthlyPayment(),
                            loan.getTermMonths(),
                            loan.getInterestRate()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_APPROVED,
                    "Loan Approved",
                    String.format("Loan approved: %.2f EUR. Payment: %.2f EUR/month for %d months.",
                            loan.getApprovedAmount(),
                            loan.getMonthlyPayment(),
                            loan.getTermMonths()));
        }

        return savedLoan;
    }

    /**
     * Reject loan
     */
    @Transactional
    public Loan rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalArgumentException("Loan is not pending");
        }

        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setRejectionReason(reason);

        Loan savedLoan = loanRepository.save(loan);

        // Send notification
        User user = loan.getUser();
        User.NotificationType notifType = user.getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_REJECTED,
                    "Loan Rejected",
                    String.format("Your loan request of %.2f EUR has been rejected. Reason: %s",
                            loan.getRequestedAmount(), reason));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_REJECTED,
                    "Loan Rejected",
                    String.format("Loan rejected: %.2f EUR. Reason: %s",
                            loan.getRequestedAmount(), reason));
        }

        return savedLoan;
    }

    /**
     * Pay loan installment
     */
    @Transactional
    public Loan payLoanInstallment(Long loanId, String accountNumber) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Loan is not approved");
        }

        if (loan.getRemainingAmount() <= 0) {
            throw new IllegalArgumentException("Loan is already paid");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Verify account belongs to user
        if (!account.getUser().getId().equals(loan.getUser().getId())) {
            throw new IllegalArgumentException("Account does not belong to user");
        }

        double paymentAmount = loan.getMonthlyPayment();

        // If remaining is less than monthly payment, pay only remaining
        if (loan.getRemainingAmount() < paymentAmount) {
            paymentAmount = loan.getRemainingAmount();
        }

        // Check if account has sufficient balance
        if (account.getBalance() < paymentAmount) {
            throw new IllegalArgumentException("Insufficient funds in account");
        }

        // Withdraw from account
        account.withdraw(paymentAmount);
        accountRepository.save(account);

        // Update loan
        loan.setRemainingAmount(loan.getRemainingAmount() - paymentAmount);
        loan.setPaidPayments(loan.getPaidPayments() + 1);

        if (loan.getRemainingAmount() <= 0) {
            loan.setStatus(Loan.LoanStatus.PAID);
        }

        Loan savedLoan = loanRepository.save(loan);

        // Send notification
        User user = loan.getUser();
        User.NotificationType notifType = user.getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_PAYMENT,
                    "Loan Payment Received",
                    String.format("Payment of %.2f EUR received. Remaining: %.2f EUR",
                            paymentAmount, loan.getRemainingAmount()));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(
                    user,
                    Notification.NotificationType.LOAN_PAYMENT,
                    "Loan Payment",
                    String.format("Payment: %.2f EUR. Remaining: %.2f EUR",
                            paymentAmount, loan.getRemainingAmount()));
        }

        return savedLoan;
    }

    /**
     * Get all loans for a customer
     */
    public List<Loan> getUserLoans(User user) {
        return loanRepository.findByUser(user);
    }

    /**
     * Get all pending loans
     */
    public List<Loan> getPendingLoans() {
        return loanRepository.findByStatus(Loan.LoanStatus.PENDING);
    }

    /**
     * Get loan by ID
     */
    public Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    }
}

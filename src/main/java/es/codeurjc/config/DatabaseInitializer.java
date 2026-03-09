package es.codeurjc.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.codeurjc.model.Account;
import es.codeurjc.model.Loan;
import es.codeurjc.model.LoanEvaluationResult;
import es.codeurjc.model.Notification;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.LoanRepository;
import es.codeurjc.repository.NotificationRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Component
@Profile("!test")
public class DatabaseInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initDatabase() {
        // Create users
        User user1 = new User(
                "customer",
                passwordEncoder.encode("Cu5t0m3r"),
                "CUSTOMER");
        user1.setFirstName("Juan");
        user1.setLastName("García");
        user1.setDni("12345678A");
        user1.setEmail("juan.garcia@email.com");
        user1.setPhone("+34600111222");
        user1.setRegistrationDate(LocalDate.now().minusYears(5));
        user1.setMonthlyIncome(2500.0);
        user1 = userRepository.save(user1);

        User user2 = new User(
                "maria",
                passwordEncoder.encode("maria123"),
                "CUSTOMER");
        user2.setFirstName("María");
        user2.setLastName("López");
        user2.setDni("87654321B");
        user2.setEmail("maria.lopez@email.com");
        user2.setPhone("+34600333444");
        user2.setRegistrationDate(LocalDate.now().minusYears(3));
        user2.setMonthlyIncome(3200.0);
        user2 = userRepository.save(user2);

        User user3 = new User(
                "manager",
                passwordEncoder.encode("M4n4g3r"),
                "MANAGER");
        user3.setFirstName("Pedro");
        user3.setLastName("Martínez");
        user3.setDni("11223344C");
        user3.setEmail("pedro.martinez@email.com");
        user3.setPhone("+34600555666");
        user3.setRegistrationDate(LocalDate.now().minusYears(1));
        user3.setMonthlyIncome(1800.0);
        user3 = userRepository.save(user3);

        // Create accounts for user 1
        Account account1 = new Account("ES0001234567", Account.AccountType.CHECKING, 5000.0);
        account1.setUser(user1);
        accountRepository.save(account1);

        Account account2 = new Account("ES0001234568", Account.AccountType.SAVINGS, 15000.0);
        account2.setUser(user1);
        accountRepository.save(account2);

        // Create accounts for user 2
        Account account3 = new Account("ES0002345678", Account.AccountType.CHECKING, 8000.0);
        account3.setUser(user2);
        accountRepository.save(account3);

        Account account4 = new Account("ES0002345679", Account.AccountType.SAVINGS, 25000.0);
        account4.setUser(user2);
        accountRepository.save(account4);

        // Create accounts for user 3
        Account account5 = new Account("ES0003456789", Account.AccountType.CHECKING, 2500.0);
        account5.setUser(user3);
        accountRepository.save(account5);

        // Create some transactions
        Transaction tx1 = new Transaction(
                account1,
                Transaction.TransactionType.DEPOSIT,
                1000.0,
                "Nómina mensual");
        tx1.setTimestamp(LocalDateTime.now().minusDays(5));
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction(
                account1,
                Transaction.TransactionType.WITHDRAWAL,
                200.0,
                "Cajero automático");
        tx2.setTimestamp(LocalDateTime.now().minusDays(3));
        transactionRepository.save(tx2);

        Transaction tx3 = new Transaction(
                account2,
                Transaction.TransactionType.DEPOSIT,
                5000.0,
                "Ahorro");
        tx3.setTimestamp(LocalDateTime.now().minusDays(10));
        transactionRepository.save(tx3);

        Transaction tx4 = new Transaction(
                account3,
                Transaction.TransactionType.TRANSFER_SENT,
                500.0,
                "Transferencia a ES0001234567");
        tx4.setDestinationAccountNumber("ES0001234567");
        tx4.setTimestamp(LocalDateTime.now().minusDays(2));
        transactionRepository.save(tx4);

        // Create a loan for user 1
        Loan loan1 = new Loan(user1, 10000.0, 60);
        loan1.setStatus(Loan.LoanStatus.APPROVED);
        loan1.setApprovedAmount(10000.0);
        loan1.setRemainingAmount(8000.0);
        loan1.setInterestRate(5.0);
        loan1.setMonthlyPayment(188.71);
        loan1.setApprovalDate(LocalDateTime.now().minusMonths(12));
        loan1.setRequestDate(LocalDateTime.now().minusMonths(12).minusDays(5));
        loan1.setTotalPayments(60);
        loan1.setPaidPayments(12);
        loan1.setLatePayments(1);

        LoanEvaluationResult eval1 = new LoanEvaluationResult(true, "Aprobado por sistema", 10000.0, 5.0, 188.71);
        loan1.setEvaluationResult(eval1);

        loanRepository.save(loan1);

        // Create a pending loan for user 2
        Loan loan2 = new Loan(user2, 20000.0, 120);
        loan2.setRequestDate(LocalDateTime.now().minusDays(2));

        LoanEvaluationResult eval2 = new LoanEvaluationResult(true, "Pre-aprobado", 20000.0, 5.0, 212.13);
        loan2.setEvaluationResult(eval2);

        loanRepository.save(loan2);

        // Create some notifications
        Notification notif1 = new Notification(
                user1,
                Notification.NotificationType.DEPOSIT,
                Notification.NotificationChannel.EMAIL,
                user1.getEmail(),
                "Bienvenido a Banking App",
                "Su cuenta ha sido creada exitosamente");
        notif1.setSentAt(LocalDateTime.now().minusDays(30));
        notif1.setSent(true);
        notificationRepository.save(notif1);

        Notification notif2 = new Notification(
                user1,
                Notification.NotificationType.LOAN_APPROVED,
                Notification.NotificationChannel.EMAIL,
                user1.getEmail(),
                "Préstamo aprobado",
                "Su préstamo de 10000.0 EUR ha sido aprobado");
        notif2.setSentAt(LocalDateTime.now().minusMonths(12));
        notif2.setSent(true);
        notificationRepository.save(notif2);

        Notification notif3 = new Notification(
                user2,
                Notification.NotificationType.TRANSFER,
                Notification.NotificationChannel.SMS,
                user2.getPhone(),
                "Transferencia recibida",
                "Ha recibido una transferencia de 500.0 EUR");
        notif3.setSentAt(LocalDateTime.now().minusDays(2));
        notif3.setSent(true);
        notificationRepository.save(notif3);

        System.out.println("✅ Database initialized with sample data");
    }
}

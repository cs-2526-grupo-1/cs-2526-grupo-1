package es.codeurjc.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.codeurjc.BankingApplication;
import es.codeurjc.model.Account;
import es.codeurjc.model.Notification;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.NotificationRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.repository.UserRepository;

@SpringBootTest(classes = BankingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferE2ETest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private Double initialBalanceAccount1Checking;
    private Double initialBalanceAccount1Savings;
    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.get("http://localhost:" + this.port);
        createTestData();
        login("testuser1", "testuser1");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        cleanTestData();
    }

    private void createTestData() {
        User testUser1 = new User(
                "Test",
                "User One",
                "67676767A",
                "testuser1@test.com",
                "600000000",
                "testuser1",
                passwordEncoder.encode("testuser1"),
                LocalDate.now(),
                3000.0,
                "CUSTOMER");
        testUser1.setNotificationType(User.NotificationType.EMAIL);
        testUser1 = userRepository.save(testUser1);

        initialBalanceAccount1Checking = 5000.0;
        Account checkingAccount = new Account("ES1111111111", Account.AccountType.CHECKING, initialBalanceAccount1Checking);
        checkingAccount.setUser(testUser1);
        checkingAccount = accountRepository.save(checkingAccount);

        initialBalanceAccount1Savings = 3000.0;
        Account savingsAccount = new Account("ES2222222222", Account.AccountType.SAVINGS, initialBalanceAccount1Savings);
        savingsAccount.setUser(testUser1);
        savingsAccount = accountRepository.save(savingsAccount);

        User testUser2 = new User(
                "Test",
                "User Two",
                "22222222B",
                "testuser2@test.com",
                "666222222",
                "testuser2",
                passwordEncoder.encode("testuser2"),
                LocalDate.now(),
                2500.0,
                "CUSTOMER");
        testUser2.setNotificationType(User.NotificationType.SMS);
        testUser2 = userRepository.save(testUser2);

        Double initialBalanceAccount2 = 2000.0;
        Account user2Account = new Account("ES3333333333", Account.AccountType.CHECKING, initialBalanceAccount2);
        user2Account.setUser(testUser2);
        user2Account = accountRepository.save(user2Account);
    }

    private void cleanTestData() {
        // Clean up test data after each test
        // As database initializer has @Profile("!test"),
        // the test data created here will only be used for
        // testing and deleting them won't delete any more data
        transactionRepository.deleteAll();
        notificationRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void login(String username, String password) {
        driver.get("http://localhost:" + this.port + "/login");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginButton")).click();
    }
}
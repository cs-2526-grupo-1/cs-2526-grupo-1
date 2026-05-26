package es.codeurjc.e2e;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.codeurjc.BankingApplication;
import es.codeurjc.model.Account;
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

    private static final String BASE_URL = E2ETestConstants.BASE_URL;
    private WebDriver driver;
    private WebDriverWait wait;
    private Double initialBalanceAccount1Checking;
    private Double initialBalanceAccount1Savings;

    @BeforeEach
    public void setUp() {
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        WebDriver driver;

        System.out.println("::notice:: testing in browser " + browser);

        switch (browser) {
            case "firefox":
                FirefoxOptions ffOptions = new FirefoxOptions();
                ffOptions.addArguments("--headless");
                driver = new FirefoxDriver(ffOptions);
                break;
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--headless");
                driver = new EdgeDriver(edgeOptions);
                break;
            case "safari":
                // safari doesn't support headless lol
                SafariOptions safariOptions = new SafariOptions();
                driver = new SafariDriver(safariOptions);
                break;
            case "chrome":
            default:
                ChromeOptions chOptions = new ChromeOptions();
                chOptions.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
                driver = new ChromeDriver(chOptions);
                break;
        }

        this.driver = driver;
        this.driver.manage().window().setSize(new Dimension(1920, 1080));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    
        createTestData();
        login(E2ETestConstants.USER1_USERNAME, E2ETestConstants.USER1_PASSWORD);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(E2ETestConstants.ID_LOGOUT_BUTTON)));
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
                E2ETestConstants.USER1_EMAIL,
                "600000000",
                E2ETestConstants.USER1_USERNAME,
                passwordEncoder.encode(E2ETestConstants.USER1_PASSWORD),
                LocalDate.now(),
                3000.0,
                "CUSTOMER");
        testUser1.setNotificationType(User.NotificationType.EMAIL);
        testUser1 = userRepository.save(testUser1);

        initialBalanceAccount1Checking = E2ETestConstants.INITIAL_BALANCE_ACCOUNT1_CHECKING;
        Account checkingAccount = new Account(E2ETestConstants.ACCOUNT_1_CHECKING, Account.AccountType.CHECKING,
                initialBalanceAccount1Checking);
        checkingAccount.setUser(testUser1);
        checkingAccount = accountRepository.save(checkingAccount);

        initialBalanceAccount1Savings = E2ETestConstants.INITIAL_BALANCE_ACCOUNT1_SAVINGS;
        Account savingsAccount = new Account(E2ETestConstants.ACCOUNT_1_SAVINGS, Account.AccountType.SAVINGS,
                initialBalanceAccount1Savings);
        savingsAccount.setUser(testUser1);
        savingsAccount = accountRepository.save(savingsAccount);

        User testUser2 = new User(
                "Test",
                "User Two",
                "22222222B",
                E2ETestConstants.USER2_EMAIL,
                "666222222",
                E2ETestConstants.USER2_USERNAME,
                passwordEncoder.encode(E2ETestConstants.USER2_PASSWORD),
                LocalDate.now(),
                2500.0,
                "CUSTOMER");
        testUser2.setNotificationType(User.NotificationType.SMS);
        testUser2 = userRepository.save(testUser2);

        Double initialBalanceAccount2 = E2ETestConstants.INITIAL_BALANCE_ACCOUNT2;
        Account user2Account = new Account(E2ETestConstants.ACCOUNT_2_CHECKING, Account.AccountType.CHECKING,
                initialBalanceAccount2);
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

    private void safeSendKeys(WebElement element, String text) {

        String browser = System.getProperty("browser", "chrome").toLowerCase();

        if ("safari".equals(browser)) {
            // sendKeys se cuelga en Safari 26 (HttpTimeoutException en SafariDriver).
            // Se asigna el valor directamente via JS; para formularios HTML planos esto
            // es equivalente a escribirlo: el valor se incluye en la submission del form.
            ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0]; const val = arguments[1];" +
                "el.value = val;" +
                "el.dispatchEvent(new Event('input',  {bubbles:true}));" +
                "el.dispatchEvent(new Event('change', {bubbles:true}));",
                element, text);
        } else {
            element.clear();
            element.sendKeys(text);
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL + this.port + E2ETestConstants.PATH_LOGIN);
        
        WebElement usernameField = driver.findElement(By.id(E2ETestConstants.ID_USERNAME));
        WebElement passwordField = driver.findElement(By.id(E2ETestConstants.ID_PASSWORD));
        
        safeSendKeys(usernameField, username);
        safeSendKeys(passwordField, password);

        // En Safari el click nativo/JS sobre el boton submit no desencadena la submission;
        // form.submit() via JS la realiza directamente e incluye todos los campos (CSRF incluido).
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        if ("safari".equals(browser)) {
            ((JavascriptExecutor) driver).executeScript("document.querySelector('form').submit();");
        } else {
            driver.findElement(By.id(E2ETestConstants.ID_LOGIN_BUTTON)).click();
        }

        wait.until(ExpectedConditions.urlContains(E2ETestConstants.PATH_DASHBOARD));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id(E2ETestConstants.ID_LOGOUT_BUTTON)
        ));
    }

    private void simulateTransfer(String fromAccount, String toAccount, double amount) {
        driver.get(BASE_URL + this.port + E2ETestConstants.PATH_TRANSFER);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(E2ETestConstants.ID_FROM_ACCOUNT)));

        Select fromAccountSelect = new Select(driver.findElement(By.id(E2ETestConstants.ID_FROM_ACCOUNT)));
        fromAccountSelect.selectByValue(fromAccount);
        
        WebElement toAccountField = driver.findElement(By.id(E2ETestConstants.ID_TO_ACCOUNT));
        WebElement amountField = driver.findElement(By.id(E2ETestConstants.ID_AMOUNT));
        
        safeSendKeys(toAccountField, toAccount);
        safeSendKeys(amountField, String.valueOf(amount));

        WebElement transferButton = driver.findElement(By.id(E2ETestConstants.ID_TRANSFER_BUTTON));
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        if ("safari".equals(browser)) {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].closest('form').submit();", transferButton);
        } else {
            transferButton.click();
        }
    }

    private void checkBalanceHasNotChanged(String accountNumber, Double initialBalance) {
        driver.get(BASE_URL + this.port + E2ETestConstants.PATH_DASHBOARD);
        String balance = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id(E2ETestConstants.ID_BALANCE_PREFIX + accountNumber))).getText();
        assertThat(Double.parseDouble(balance)).isCloseTo(initialBalance, within(0.000001));

        // Despite balance is tested in unit tests, we can also check
        // it here to ensure it has not changed
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        assertThat(account.getBalance()).isEqualTo(initialBalance);
    }

    private void waitForDashboard() {
        wait.until(ExpectedConditions.urlContains(E2ETestConstants.PATH_DASHBOARD));
    }

    private double getAccountBalance(String accountSuffix) {
        String balanceText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_BALANCE_PREFIX + accountSuffix))).getText();
        return Double.parseDouble(balanceText);
    }

    private void verifySuccessMessage(String expectedMessage) {
        String successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("alert-success"))).getText();
        assertThat(successMessage).contains(expectedMessage);
    }

    private void reloginAs(String username, String password) {
        driver.findElement(By.id(E2ETestConstants.ID_LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(E2ETestConstants.ID_LOGIN_BUTTON)));
        login(username, password);
    }

    @Test
    public void test1_makeTransferBetweenOwnAccounts() {
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_1_SAVINGS;
        double amount = E2ETestConstants.AMOUNT_TO_TRANSFER;

        simulateTransfer(fromAccount, toAccount, amount);
        waitForDashboard();

        assertThat(getAccountBalance(fromAccount))
                .isCloseTo(initialBalanceAccount1Checking - amount, within(0.000001));

        assertThat(getAccountBalance(toAccount))
                .isCloseTo(initialBalanceAccount1Savings + amount, within(0.000001));

        verifySuccessMessage(E2ETestConstants.TRANSFER_SUCCESS);
    }

    @Test
    public void test2_makeSuccessfulTransferBetweenUsers() {
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_2_CHECKING;
        double amount = E2ETestConstants.AMOUNT_TO_TRANSFER;

        double expectedBalanceUser1 = E2ETestConstants.INITIAL_BALANCE_ACCOUNT1_CHECKING - amount;
        double expectedBalanceUser2 = E2ETestConstants.INITIAL_BALANCE_ACCOUNT2 + amount;

        simulateTransfer(fromAccount, toAccount, amount);
        waitForDashboard();

        assertThat(getAccountBalance(fromAccount))
                .isCloseTo(expectedBalanceUser1, within(0.01));

        reloginAs(E2ETestConstants.USER2_USERNAME, E2ETestConstants.USER2_PASSWORD);

        assertThat(getAccountBalance(toAccount))
                .isCloseTo(expectedBalanceUser2, within(0.01));
    }

    @Test
    public void test3_makeTransferToSameAccount() {

        // The same account is assigned to the source and destination account
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_1_CHECKING;

        simulateTransfer(fromAccount, toAccount, E2ETestConstants.STANDARD_AMOUNT);

        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_ERROR_MESSAGE))).getText();

        // Check that the error message coincides with the expected one
        assertThat(errorMessage).isEqualTo(E2ETestConstants.ERROR_SAME_ACCOUNT);

        // Check that the balance of the source account has not changed (in the dashboard, as getBalance is tested in unit tests)
        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);

        // Relogin and check that the source amount has not changed, once again
        reloginAs(E2ETestConstants.USER1_USERNAME, E2ETestConstants.USER1_PASSWORD);

        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);
    }

    @Test
    public void test4_makeTransferWithExceedingAmount() {
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_2_CHECKING;

        // Value exceeding the balance of the source account
        int amount = E2ETestConstants.INITIAL_BALANCE_ACCOUNT1_CHECKING.intValue() + 1;

        simulateTransfer(fromAccount, toAccount, amount);
        // We can also check that the balance of the source account has not changed

        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_ERROR_MESSAGE))).getText();

        // Check that the error message shown is the expected one
        assertThat(errorMessage).isEqualTo(E2ETestConstants.ERROR_INSUFFICIENT_FUNDS);

        // Check that the balance of the source account has not changed (in the
        // dashboard,
        // as getBalance is tested in unit tests)
        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);

        reloginAs(E2ETestConstants.USER2_USERNAME, E2ETestConstants.USER2_PASSWORD);
        checkBalanceHasNotChanged(toAccount, E2ETestConstants.INITIAL_BALANCE_ACCOUNT2);
    }

    @Test
    public void test5_makeTransferWithNegativeAmount() {
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_2_CHECKING;
        double amount = E2ETestConstants.NEGATIVE_AMOUNT;

        simulateTransfer(fromAccount, toAccount, amount);

        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_ERROR_MESSAGE))).getText();

        // Check that the error message shown is the expected one
        assertThat(errorMessage).isEqualTo(E2ETestConstants.ERROR_NEGATIVE_AMOUNT);

        // Check that the balance of the source account has not changed (in the
        // dashboard,
        // as getBalance is tested in unit tests)
        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);

        reloginAs(E2ETestConstants.USER2_USERNAME, E2ETestConstants.USER2_PASSWORD);
        checkBalanceHasNotChanged(toAccount, E2ETestConstants.INITIAL_BALANCE_ACCOUNT2);
    }

    @Test
    public void test6_makeTransferWithExceedingAmount() {
        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_2_CHECKING;
        double amount = E2ETestConstants.EXCEEDING_AMOUNT;

        simulateTransfer(fromAccount, toAccount, amount);
        // We can also check that the balance of the source account has not changed
        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_ERROR_MESSAGE))).getText();

        // Check that the error message shown is the expected one
        assertThat(errorMessage).isEqualTo(E2ETestConstants.ERROR_EXCEEDS_LIMIT);

        // Check that the balance of the source account has not changed (in the
        // dashboard,
        // as getBalance is tested in unit tests)
        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);

        reloginAs(E2ETestConstants.USER2_USERNAME, E2ETestConstants.USER2_PASSWORD);
        checkBalanceHasNotChanged(toAccount, E2ETestConstants.INITIAL_BALANCE_ACCOUNT2);
    }

    @Test
    public void test7_makeTransferToUnexistingAccount() {

        String fromAccount = E2ETestConstants.ACCOUNT_1_CHECKING;
        String toAccount = E2ETestConstants.ACCOUNT_NOT_EXISTING;
        int amount = E2ETestConstants.STANDARD_AMOUNT_INT;

        simulateTransfer(fromAccount, toAccount, amount);

        // Wait until we remian on the transfer page after submitting the form
        wait.until(ExpectedConditions.urlContains(E2ETestConstants.PATH_TRANSFER));

        // Wait until the error message is visible
        String errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id(E2ETestConstants.ID_ERROR_MESSAGE)))
                .getText();

        assertThat(errorMessage).isEqualTo(E2ETestConstants.ERROR_ACCOUNT_NOT_FOUND);

        // Wait until the dashbord elements are fully loaded before checking the balance
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id(E2ETestConstants.ID_LOGOUT_BUTTON)));

        checkBalanceHasNotChanged(fromAccount, initialBalanceAccount1Checking);
    }
}

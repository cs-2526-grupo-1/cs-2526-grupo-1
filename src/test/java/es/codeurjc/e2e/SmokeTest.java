package es.codeurjc.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import es.codeurjc.BankingApplication;

@SpringBootTest(classes = BankingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private String expectedVersion;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--lang=en-US");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        String customUrl = System.getProperty("app.url");
        if (customUrl != null && !customUrl.isEmpty()) {
            baseUrl = customUrl;
        } else {
            baseUrl = E2ETestConstants.BASE_URL + this.port;
        }

        expectedVersion = System.getProperty("app.version", "1.0.0");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testApplicationDeploymentVersion() {
        driver.get(baseUrl + E2ETestConstants.PATH_LOGIN);
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(E2ETestConstants.ID_USERNAME)));
        
        String actualVersionText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("text-muted"))).getText().trim();
        
        assertThat(actualVersionText).contains("v" + expectedVersion);
    }
}

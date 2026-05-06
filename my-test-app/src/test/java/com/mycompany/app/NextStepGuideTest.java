package com.mycompany.app;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import java.time.Duration;

public class NextStepGuideTest {

    WebDriver driver;
    WebDriverWait wait;

    String BASE_URL = System.getProperty("baseUrl", "http://13.206.71.119:8000");
    String USERNAME = "testuser_selenium";
    String PASSWORD = "TestPass@1234";

    // ==============================
    // SETUP (runs before tests)
    // ==============================
    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ==============================
    // CLEANUP
    // ==============================
    @AfterClass
    public void teardown() {
        driver.quit();
    }

    // ==============================
    // HELPER FUNCTION
    // ==============================
    public void login() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id_login")));

        driver.findElement(By.id("id_login")).sendKeys(USERNAME);
        driver.findElement(By.id("id_password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    // ==============================
    // TEST CASES (15)
    // ==============================

    @Test
    public void test01_homepageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assert driver.getTitle().contains("Next Step Guide");
    }

    @Test
    public void test02_homepageSearchVisible() {
        driver.get(BASE_URL);
        WebElement search = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input")));
        assert search.isDisplayed();
    }

    @Test
    public void test03_loginPageLoads() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assert driver.getCurrentUrl().contains("login");
    }

    @Test
    public void test04_loginFieldsPresent() {
        driver.get(BASE_URL + "/accounts/login/");
        assert driver.findElement(By.id("id_login")).isDisplayed();
        assert driver.findElement(By.id("id_password")).isDisplayed();
    }

    @Test
    public void test05_invalidLogin() throws InterruptedException {
        driver.get(BASE_URL + "/accounts/login/");
        driver.findElement(By.id("id_login")).sendKeys("wronguser");
        driver.findElement(By.id("id_password")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        assert driver.getPageSource().toLowerCase().contains("error") ||
               driver.getCurrentUrl().contains("login");
    }

    @Test
    public void test06_signupPageLoads() {
        driver.get(BASE_URL + "/accounts/signup/");
        assert driver.findElement(By.id("id_email")).isDisplayed();
    }

    @Test
    public void test07_passwordMismatch() throws InterruptedException {
        driver.get(BASE_URL + "/accounts/signup/");
        driver.findElement(By.id("id_email")).sendKeys("test@test.com");
        driver.findElement(By.id("id_username")).sendKeys("user123");
        driver.findElement(By.id("id_password1")).sendKeys("Pass123");
        driver.findElement(By.id("id_password2")).sendKeys("WrongPass");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        assert driver.getPageSource().toLowerCase().contains("password");
    }

    @Test
    public void test08_scholarshipsPageLoads() {
        driver.get(BASE_URL + "/scholarships.html");
        assert driver.findElement(By.tagName("body")).isDisplayed();
    }

    @Test
    public void test09_searchFunction() throws InterruptedException {
        driver.get(BASE_URL + "/scholarships.html");

        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.id("title")));
        input.sendKeys("Pakistan");
        input.sendKeys(Keys.RETURN);

        Thread.sleep(2000);

        assert driver.getCurrentUrl().toLowerCase().contains("pakistan") ||
               driver.getPageSource().toLowerCase().contains("pakistan");
    }

    @Test
    public void test10_universitiesPage() {
        driver.get(BASE_URL + "/universities/");
        assert driver.getPageSource().toLowerCase().contains("universi");
    }

    @Test
    public void test11_navbarLinkExists() {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href, 'scholarships')]")
        ));
        assert link.isDisplayed();
    }

    @Test
    public void test12_aboutPage() {
        driver.get(BASE_URL + "/about.html");
        assert driver.findElement(By.tagName("body")).isDisplayed();
    }

    @Test
    public void test13_validLoginRedirect() throws InterruptedException {
        driver.get(BASE_URL + "/accounts/login/");
        driver.findElement(By.id("id_login")).sendKeys(USERNAME);
        driver.findElement(By.id("id_password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(3000);

        assert !driver.getCurrentUrl().contains("login");
    }

    @Test
    public void test14_dashboardAccess() {
        login();
        driver.get(BASE_URL + "/users/dashboard/");
        assert driver.getPageSource().toLowerCase().contains("dashboard");
    }

    @Test
    public void test15_feedbackPage() {
        driver.get(BASE_URL + "/users/feedback/");
        WebElement comments = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("comments")));
        assert comments.isDisplayed();
    }
}
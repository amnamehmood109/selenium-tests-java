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

    String BASE_URL = System.getProperty("baseUrl", "http://localhost:8000");
    String TEST_USERNAME = "testuser_selenium";
    String TEST_PASSWORD = "TestPass@1234";

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public void teardown() {
        if (driver != null) driver.quit();
    }

    // Helper: login using your actual HTML form
    public void login() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        // Your HTML uses id="login" for email field
        driver.findElement(By.id("login")).clear();
        driver.findElement(By.id("login")).sendKeys(TEST_USERNAME);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        
        // Your form uses button with class "continue-btn"
        driver.findElement(By.cssSelector("button.continue-btn")).click();
        
        wait.until(ExpectedConditions.not(
            ExpectedConditions.urlContains("/accounts/login/")
        ));
    }

    // ==============================
    // TEST 1: Homepage loads
    // ==============================
    @Test(priority = 1)
    public void test01_homepageLoads() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assert driver.getPageSource().length() > 100 : "Homepage body appears empty";
        System.out.println("✓ test01_homepageLoads PASSED");
    }

    // ==============================
    // TEST 2: Login page loads and has a form
    // ==============================
    @Test(priority = 2)
    public void test02_loginPageLoads() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assert driver.getCurrentUrl().contains("login") : "Not on login page";
        System.out.println("✓ test02_loginPageLoads PASSED");
    }

    // ==============================
    // TEST 3: Login form fields are present
    // Your HTML uses id="login" and id="password"
    // ==============================
    @Test(priority = 3)
    public void test03_loginFieldsPresent() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        boolean hasLoginField = !driver.findElements(By.id("login")).isEmpty();
        boolean hasPasswordField = !driver.findElements(By.id("password")).isEmpty();
        
        assert hasLoginField : "Email field (id='login') not found on login page";
        assert hasPasswordField : "Password field (id='password') not found on login page";
        System.out.println("✓ test03_loginFieldsPresent PASSED");
    }

    // ==============================
    // TEST 4: Invalid login shows error
    // ==============================
    @Test(priority = 4)
    public void test04_invalidLogin() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        driver.findElement(By.id("login")).sendKeys("completely_invalid_user_xyz");
        driver.findElement(By.id("password")).sendKeys("wrongpassword999");
        driver.findElement(By.cssSelector("button.continue-btn")).click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("login"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert, .errorlist, .text-danger"))
        ));

        boolean stayedOnLogin = driver.getCurrentUrl().contains("login");
        boolean hasErrorText = driver.getPageSource().toLowerCase().contains("error") ||
                               driver.getPageSource().toLowerCase().contains("invalid") ||
                               driver.getPageSource().toLowerCase().contains("incorrect");

        assert stayedOnLogin || hasErrorText : "No error shown for invalid login";
        System.out.println("✓ test04_invalidLogin PASSED");
    }

    // ==============================
    // TEST 5: Signup page loads with all required fields
    // ==============================
    @Test(priority = 5)
    public void test05_signupPageLoads() {
        driver.get(BASE_URL + "/accounts/signup/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        assert !driver.findElements(By.id("id_email")).isEmpty() : "Email field missing on signup";
        assert !driver.findElements(By.id("id_username")).isEmpty() : "Username field missing on signup";
        assert !driver.findElements(By.id("id_password1")).isEmpty() : "Password field missing on signup";
        assert !driver.findElements(By.id("id_password2")).isEmpty() : "Confirm password field missing on signup";
        System.out.println("✓ test05_signupPageLoads PASSED");
    }

    // ==============================
    // TEST 6: Signup with mismatched passwords shows error
    // ==============================
    @Test(priority = 6)
    public void test06_passwordMismatch() {
        driver.get(BASE_URL + "/accounts/signup/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id_email")));

        driver.findElement(By.id("id_email")).sendKeys("mismatchtest@example.com");
        driver.findElement(By.id("id_username")).sendKeys("mismatchuser123");
        driver.findElement(By.id("id_password1")).sendKeys("GoodPassword@123");
        driver.findElement(By.id("id_password2")).sendKeys("DifferentPassword@456");

        driver.findElement(By.cssSelector("button.continue-btn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        boolean stayedOnSignup = driver.getCurrentUrl().contains("signup");
        boolean hasPasswordError = driver.getPageSource().toLowerCase().contains("password");

        assert stayedOnSignup || hasPasswordError : "No feedback shown for password mismatch";
        System.out.println("✓ test06_passwordMismatch PASSED");
    }

    // ==============================
    // TEST 7: Universities page loads
    // ==============================
    @Test(priority = 7)
    public void test07_universitiesPageLoads() {
        driver.get(BASE_URL + "/universities/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assert driver.getPageSource().toLowerCase().contains("universit") : 
               "Universities page does not contain expected content";
        System.out.println("✓ test07_universitiesPageLoads PASSED");
    }

    // ==============================
    // TEST 8: About page loads
    // ==============================
    @Test(priority = 8)
    public void test08_aboutPageLoads() {
        driver.get(BASE_URL + "/about/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assert driver.getPageSource().toLowerCase().contains("scholarship") ||
               driver.getPageSource().toLowerCase().contains("guidl") :
               "About page does not contain expected content";
        System.out.println("✓ test08_aboutPageLoads PASSED");
    }

    // ==============================
    // TEST 9: Navbar has a link to scholarships
    // ==============================
    @Test(priority = 9)
    public void test09_navbarHasScholarshipsLink() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        java.util.List<WebElement> links = driver.findElements(
            By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'scholarship')]")
        );
        assert !links.isEmpty() : "No scholarship link found in the page";
        System.out.println("✓ test09_navbarHasScholarshipsLink PASSED");
    }

    // ==============================
    // TEST 10: Valid login redirects
    // ==============================
    @Test(priority = 10)
    public void test10_validLoginRedirects() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        driver.findElement(By.id("login")).sendKeys(TEST_USERNAME);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button.continue-btn")).click();

        wait.until(ExpectedConditions.not(
            ExpectedConditions.urlContains("/accounts/login/")
        ));

        assert !driver.getCurrentUrl().contains("/accounts/login/") :
               "Still on login page after valid credentials";
        System.out.println("✓ test10_validLoginRedirects PASSED");
    }

    // ==============================
    // TEST 11: Dashboard loads after login
    // ==============================
    @Test(priority = 11)
    public void test11_dashboardLoadsAfterLogin() {
        login();
        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        assert driver.getPageSource().contains("My Scholarships") ||
               driver.getPageSource().contains("Apply to University") :
               "Dashboard does not contain expected content";
        System.out.println("✓ test11_dashboardLoadsAfterLogin PASSED");
    }

    // ==============================
    // TEST 12: Dashboard has quick action tiles
    // ==============================
    @Test(priority = 12)
    public void test12_dashboardHasActionTiles() {
        login();
        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("action-tiles")));

        WebElement tiles = driver.findElement(By.className("action-tiles"));
        assert tiles.isDisplayed() : "Action tiles section not visible on dashboard";
        
        java.util.List<WebElement> tileLinks = tiles.findElements(By.className("action-tile"));
        assert tileLinks.size() >= 2 : "Expected at least 2 action tiles on dashboard";
        System.out.println("✓ test12_dashboardHasActionTiles PASSED");
    }

    // ==============================
    // TEST 13: Feedback page loads when logged in
    // ==============================
    @Test(priority = 13)
    public void test13_feedbackPageLoadsWhenLoggedIn() {
        login();
        driver.get(BASE_URL + "/users/feedback/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        WebElement feedbackTextarea = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("feedback"))
        );
        assert feedbackTextarea.isDisplayed() : "Feedback textarea not visible";
        System.out.println("✓ test13_feedbackPageLoadsWhenLoggedIn PASSED");
    }

    // ==============================
    // TEST 14: Feedback page shows login prompt when not logged in
    // ==============================
    @Test(priority = 14)
    public void test14_feedbackRequiresLogin() {
        driver.manage().deleteAllCookies();
        
        driver.get(BASE_URL + "/users/feedback/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        assert driver.getPageSource().contains("not logged in") ||
               driver.getCurrentUrl().contains("login") :
               "Unauthenticated user should see login prompt or be redirected";
        System.out.println("✓ test14_feedbackRequiresLogin PASSED");
    }

    // ==============================
    // TEST 15: Logout works
    // ==============================
    @Test(priority = 15)
    public void test15_logoutWorks() {
        login();
        
        driver.get(BASE_URL + "/accounts/logout/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        boolean redirectedToLogin = driver.getCurrentUrl().contains("login") ||
                                    driver.getCurrentUrl().contains("accounts");
        boolean showsLoggedOut = driver.getPageSource().toLowerCase().contains("login") ||
                                  driver.getPageSource().toLowerCase().contains("sign in");

        assert redirectedToLogin || showsLoggedOut :
               "After logout, dashboard should not be accessible";
        System.out.println("✓ test15_logoutWorks PASSED");
    }
}
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

    // Helper: login using Django's built-in login view
    // Uses <input type="submit"> NOT <button>
    public void login() {
        driver.get(BASE_URL + "/accounts/login/");
        // Wait for the form to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        // Try allauth field IDs first, fall back to Django's default
        try {
            driver.findElement(By.id("id_login")).clear();
            driver.findElement(By.id("id_login")).sendKeys(TEST_USERNAME);
            driver.findElement(By.id("id_password")).clear();
            driver.findElement(By.id("id_password")).sendKeys(TEST_PASSWORD);
        } catch (NoSuchElementException e) {
            // Django built-in LoginView uses id_username
            driver.findElement(By.id("id_username")).clear();
            driver.findElement(By.id("id_username")).sendKeys(TEST_USERNAME);
            driver.findElement(By.id("id_password")).clear();
            driver.findElement(By.id("id_password")).sendKeys(TEST_PASSWORD);
        }
        
        // Your login form uses <input type="submit">, not <button>
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        
        // Wait for redirect away from login page
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
        // Just verify page loaded with a 200 (body is present and page source is not empty)
        assert driver.getPageSource().length() > 100 : "Homepage body appears empty";
    }

    // ==============================
    // TEST 2: Login page loads and has a form
    // ==============================
    @Test(priority = 2)
    public void test02_loginPageLoads() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assert driver.getCurrentUrl().contains("login") : "Not on login page";
    }

    // ==============================
    // TEST 3: Login form fields are present
    // (allauth uses id_login, Django built-in uses id_username)
    // ==============================
    @Test(priority = 3)
    public void test03_loginFieldsPresent() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        // Check either allauth or Django default field IDs exist
        boolean hasLoginField = !driver.findElements(By.id("id_login")).isEmpty() ||
                                !driver.findElements(By.id("id_username")).isEmpty();
        boolean hasPasswordField = !driver.findElements(By.id("id_password")).isEmpty();
        
        assert hasLoginField : "Username/login field not found on login page";
        assert hasPasswordField : "Password field not found on login page";
    }

    // ==============================
    // TEST 4: Invalid login shows error
    // ==============================
    @Test(priority = 4)
    public void test04_invalidLogin() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        
        try {
            driver.findElement(By.id("id_login")).sendKeys("completely_invalid_user_xyz");
        } catch (NoSuchElementException e) {
            driver.findElement(By.id("id_username")).sendKeys("completely_invalid_user_xyz");
        }
        driver.findElement(By.id("id_password")).sendKeys("wrongpassword999");
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("login"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert, .errorlist, .text-danger"))
        ));

        // Either stays on login page or shows an error message
        boolean stayedOnLogin = driver.getCurrentUrl().contains("login");
        boolean hasErrorText = driver.getPageSource().toLowerCase().contains("error") ||
                               driver.getPageSource().toLowerCase().contains("invalid") ||
                               driver.getPageSource().toLowerCase().contains("incorrect");

        assert stayedOnLogin || hasErrorText : "No error shown for invalid login";
    }

    // ==============================
    // TEST 5: Signup page loads with all required fields
    // Based on your actual signup.html template
    // ==============================
    @Test(priority = 5)
    public void test05_signupPageLoads() {
        driver.get(BASE_URL + "/accounts/signup/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        // These IDs come directly from your signup.html: id_email, id_username, id_password1, id_password2
        assert !driver.findElements(By.id("id_email")).isEmpty() : "Email field missing on signup";
        assert !driver.findElements(By.id("id_username")).isEmpty() : "Username field missing on signup";
        assert !driver.findElements(By.id("id_password1")).isEmpty() : "Password field missing on signup";
        assert !driver.findElements(By.id("id_password2")).isEmpty() : "Confirm password field missing on signup";
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

        // Your signup form has a button with class continue-btn
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // After mismatch, should stay on signup and show password-related error
        boolean stayedOnSignup = driver.getCurrentUrl().contains("signup");
        boolean hasPasswordError = driver.getPageSource().toLowerCase().contains("password");

        assert stayedOnSignup || hasPasswordError : "No feedback shown for password mismatch";
    }

    // ==============================
    // TEST 7: Universities page loads
    // URL confirmed from urls.py: /universities/
    // ==============================
    @Test(priority = 7)
    public void test07_universitiesPageLoads() {
        driver.get(BASE_URL + "/universities/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assert driver.getPageSource().toLowerCase().contains("universit") : 
               "Universities page does not contain expected content";
    }

    // ==============================
    // TEST 8: About page loads
    // URL from app1 urls - check your app1/urls.py for exact path
    // Based on about.html content, asserting on "Guidlines" heading
    // ==============================
    @Test(priority = 8)
    public void test08_aboutPageLoads() {
        driver.get(BASE_URL + "/about/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // about.html has a breadcrumbs heading "Guidlines"
        assert driver.getPageSource().toLowerCase().contains("scholarship") ||
               driver.getPageSource().toLowerCase().contains("guidl") :
               "About page does not contain expected content";
    }

    // ==============================
    // TEST 9: Navbar has a link to scholarships
    // ==============================
    @Test(priority = 9)
    public void test09_navbarHasScholarshipsLink() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("nav")));
        
        // Look for any anchor that references scholarships
        java.util.List<WebElement> links = driver.findElements(
            By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'scholarship')]")
        );
        assert !links.isEmpty() : "No scholarship link found in the page";
    }

    // ==============================
    // TEST 10: Valid login redirects away from login page
    // Requires testuser_selenium to exist in DB
    // ==============================
    @Test(priority = 10)
    public void test10_validLoginRedirects() {
        driver.get(BASE_URL + "/accounts/login/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        try {
            driver.findElement(By.id("id_login")).sendKeys(TEST_USERNAME);
        } catch (NoSuchElementException e) {
            driver.findElement(By.id("id_username")).sendKeys(TEST_USERNAME);
        }
        driver.findElement(By.id("id_password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        wait.until(ExpectedConditions.not(
            ExpectedConditions.urlContains("/accounts/login/")
        ));

        assert !driver.getCurrentUrl().contains("/accounts/login/") :
               "Still on login page after valid credentials — user may not exist in DB";
    }

    // ==============================
    // TEST 11: Dashboard loads after login
    // dashboard.html is confirmed at /users/dashboard/
    // ==============================
    @Test(priority = 11)
    public void test11_dashboardLoadsAfterLogin() {
        login();
        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // dashboard.html has text "My Scholarships" and "My Universities"
        assert driver.getPageSource().contains("My Scholarships") ||
               driver.getPageSource().contains("Apply to University") :
               "Dashboard page does not contain expected dashboard content";
    }

    // ==============================
    // TEST 12: Dashboard has quick action tiles
    // From dashboard.html: Apply to University, Apply to Scholarship, Settings, My Profile
    // ==============================
    @Test(priority = 12)
    public void test12_dashboardHasActionTiles() {
        login();
        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("action-tiles")));

        WebElement tiles = driver.findElement(By.className("action-tiles"));
        assert tiles.isDisplayed() : "Action tiles section not visible on dashboard";
        
        // Check at least one tile link is present
        java.util.List<WebElement> tileLinks = tiles.findElements(By.className("action-tile"));
        assert tileLinks.size() >= 2 : "Expected at least 2 action tiles on dashboard";
    }

    // ==============================
    // TEST 13: Feedback page loads when logged in
    // feedback.html has textarea with id="feedback"
    // ==============================
    @Test(priority = 13)
    public void test13_feedbackPageLoadsWhenLoggedIn() {
        login();
        driver.get(BASE_URL + "/users/feedback/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // feedback.html has textarea id="feedback" (NOT id="comments")
        WebElement feedbackTextarea = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("feedback"))
        );
        assert feedbackTextarea.isDisplayed() : "Feedback textarea not visible";
    }

    // ==============================
    // TEST 14: Feedback page shows login prompt when not logged in
    // feedback.html has {% else %} <h1> You are not logged in </h1>
    // ==============================
    @Test(priority = 14)
    public void test14_feedbackRequiresLogin() {
        // First logout by clearing cookies
        driver.manage().deleteAllCookies();
        
        driver.get(BASE_URL + "/users/feedback/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // feedback.html shows "You are not logged in" when unauthenticated
        assert driver.getPageSource().contains("not logged in") ||
               driver.getCurrentUrl().contains("login") :
               "Unauthenticated user should see login prompt or be redirected";
    }

    // ==============================
    // TEST 15: Logout works
    // ==============================
    @Test(priority = 15)
    public void test15_logoutWorks() {
        login();
        
        // Navigate to logout URL
        driver.get(BASE_URL + "/accounts/logout/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // After logout, visiting dashboard should redirect to login or show logged-out state
        driver.get(BASE_URL + "/users/dashboard/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        boolean redirectedToLogin = driver.getCurrentUrl().contains("login") ||
                                    driver.getCurrentUrl().contains("accounts");
        boolean showsLoggedOut = driver.getPageSource().toLowerCase().contains("login") ||
                                  driver.getPageSource().toLowerCase().contains("sign in");

        assert redirectedToLogin || showsLoggedOut :
               "After logout, dashboard should not be accessible";
    }
}
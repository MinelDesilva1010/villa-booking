package com.villastay.tests;

import com.villastay.base.BaseTest;
import com.villastay.pages.HomePage;
import com.villastay.pages.LoginPage;
import com.villastay.utils.ConfigReader;
import com.villastay.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Covers the signup/login flow in Login.jsx against the live deployed
 * backend (there is no test/staging environment). Because of that:
 *  - Every signup uses a freshly generated email (see TestDataGenerator).
 *  - Tests that need an existing account depend on testSuccessfulSignup
 *    via dependsOnMethods, so TestNG guarantees ordering even though a
 *    fresh WebDriver is created per test method.
 */
public class LoginSignupTest extends BaseTest {

    private String sharedName;
    private String sharedEmail;
    private final String sharedPassword = "TestPass123!";

    @BeforeClass
    public void generateSharedCredentials() {
        sharedName = TestDataGenerator.uniqueName();
        sharedEmail = TestDataGenerator.uniqueEmail();
    }

    @Test(description = "New user can sign up and lands signed-in on the homepage")
    public void testSuccessfulSignup() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());

        HomePage homePage = loginPage.signUp(sharedName, sharedEmail, sharedPassword);

        Assert.assertTrue(homePage.isSignedIn(), "Expected user to be signed in after signup");
        Assert.assertTrue(
                homePage.getGreetingText().contains(sharedName),
                "Expected navbar greeting to contain the signed-up user's name"
        );
    }

    @Test(description = "Signing up again with the same email shows a duplicate-email error",
            dependsOnMethods = "testSuccessfulSignup")
    public void testDuplicateSignupShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());
        loginPage.switchToSignupMode();

        loginPage.enterName(TestDataGenerator.uniqueName());
        loginPage.enterEmail(sharedEmail); // reuse existing email
        loginPage.enterPassword(sharedPassword);
        loginPage.submitExpectingFailure();

        Assert.assertTrue(
                loginPage.getErrorText().toLowerCase().contains("already registered"),
                "Expected a duplicate-email error message, got: " + loginPage.getErrorText()
        );
    }

    @Test(description = "Existing user can log in with correct credentials",
            dependsOnMethods = "testSuccessfulSignup")
    public void testSuccessfulLoginWithValidCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());

        HomePage homePage = loginPage.logIn(sharedEmail, sharedPassword);

        Assert.assertTrue(homePage.isSignedIn(), "Expected user to be signed in after login");
    }

    @Test(description = "Login with the correct email but wrong password shows an error",
            dependsOnMethods = "testSuccessfulSignup")
    public void testLoginWithWrongPasswordShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());
        loginPage.switchToLoginMode();

        loginPage.enterEmail(sharedEmail);
        loginPage.enterPassword("TheWrongPassword!");
        loginPage.submitExpectingFailure();

        Assert.assertTrue(
                loginPage.getErrorText().toLowerCase().contains("invalid email or password"),
                "Expected invalid-credentials error, got: " + loginPage.getErrorText()
        );
    }

    @Test(description = "Login with an email that was never registered shows an error")
    public void testLoginWithUnregisteredEmailShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());
        loginPage.switchToLoginMode();

        loginPage.enterEmail(TestDataGenerator.uniqueEmail()); // guaranteed not registered
        loginPage.enterPassword("SomePassword123!");
        loginPage.submitExpectingFailure();

        Assert.assertTrue(
                loginPage.getErrorText().toLowerCase().contains("invalid email or password"),
                "Expected invalid-credentials error, got: " + loginPage.getErrorText()
        );
    }

    @Test(description = "The form heading toggles correctly between sign-in and sign-up modes")
    public void testToggleBetweenSignInAndSignUpModes() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());

        Assert.assertEquals(loginPage.getHeadingText(), "Sign in");

        loginPage.switchToSignupMode();
        Assert.assertEquals(loginPage.getHeadingText(), "Create account");
        Assert.assertTrue(loginPage.isInSignupMode(), "Name field should appear in signup mode");

        loginPage.switchToLoginMode();
        Assert.assertEquals(loginPage.getHeadingText(), "Sign in");
        Assert.assertFalse(loginPage.isInSignupMode(), "Name field should disappear in login mode");
    }

    @Test(description = "A signed-in user can sign out from the homepage navbar",
            dependsOnMethods = "testSuccessfulSignup")
    public void testSignOutAfterLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());
        HomePage homePage = loginPage.logIn(sharedEmail, sharedPassword);
        Assert.assertTrue(homePage.isSignedIn(), "Precondition failed: user should be signed in");

        homePage.clickSignOut();

        Assert.assertFalse(homePage.isSignedIn(), "Expected user to be signed out");
    }
}

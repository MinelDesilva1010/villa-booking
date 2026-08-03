package com.villastay.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Login.jsx renders ONE form that toggles between "Sign in" and "Sign up"
 * mode via component state (no page navigation, no ids on inputs).
 *
 * Locator notes (no data-testid available in the source):
 *  - Name field only exists in DOM when in signup mode -> guard with isPresent().
 *  - The submit button's visible text changes ("Sign in" / "Create account" /
 *    "Please wait...") so we locate it by type="submit" instead of text.
 *  - The toggle link is the only <span> inside the <p> that follows the
 *    <form>, so we locate it structurally rather than by its (changing) text.
 */
public class LoginPage extends BasePage {

    private static final String LOGIN_PATH = "/login";

    private final By nameInput = By.cssSelector("input[placeholder='Your name']");
    private final By emailInput = By.cssSelector("input[type='email']");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By toggleModeLink = By.xpath("//form/following-sibling::p/span");
    private final By errorMessage = By.xpath("//p[contains(@style,'color: red') or contains(@style,'color:red')]");
    private final By pageHeading = By.cssSelector("h2.section-title");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + LOGIN_PATH);
        visible(emailInput);
    }

    public boolean isInSignupMode() {
        return isPresent(nameInput);
    }

    /** Clicks the toggle link if the form isn't already in the desired mode. */
    public LoginPage switchToSignupMode() {
        if (!isInSignupMode()) {
            clickable(toggleModeLink).click();
            visible(nameInput);
        }
        return this;
    }

    public LoginPage switchToLoginMode() {
        if (isInSignupMode()) {
            clickable(toggleModeLink).click();
            wait.until(d -> !isInSignupMode());
        }
        return this;
    }

    public LoginPage enterName(String name) {
        type(nameInput, name);
        return this;
    }

    public LoginPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    /** Submits the form and returns HomePage, assuming success navigates to "/". */
    public HomePage submitExpectingSuccess() {
        clickable(submitButton).click();
        wait.until(d -> !d.getCurrentUrl().contains(LOGIN_PATH));
        return new HomePage(driver);
    }

    /** Submits the form and stays on LoginPage, for negative-path assertions. */
    public LoginPage submitExpectingFailure() {
        clickable(submitButton).click();
        visible(errorMessage);
        return this;
    }

    public String getErrorText() {
        return visible(errorMessage).getText();
    }

    public boolean hasError() {
        return isPresent(errorMessage);
    }

    public String getHeadingText() {
        return visible(pageHeading).getText();
    }

    /** Convenience: fill and submit a signup form in one call. */
    public HomePage signUp(String name, String email, String password) {
        switchToSignupMode();
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        return submitExpectingSuccess();
    }

    /** Convenience: fill and submit a login form in one call. */
    public HomePage logIn(String email, String password) {
        switchToLoginMode();
        enterEmail(email);
        enterPassword(password);
        return submitExpectingSuccess();
    }
}

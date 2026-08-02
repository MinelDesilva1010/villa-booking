package com.villastay.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.SkipException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Home.jsx navbar has no ids/data-testid, so both "Sign in" and "Sign out"
 * share the same class ("nav-btn"). We locate by visible button text instead.
 */
public class HomePage extends BasePage {

    private final By signInButton = By.xpath("//button[normalize-space()='Sign in']");
    private final By signOutButton = By.xpath("//button[normalize-space()='Sign out']");
    private final By greeting = By.xpath("//span[contains(text(), '\uD83D\uDC4B')]"); // 👋 emoji span

    // --- Search + villa grid ---
    private final By destinationInput = By.cssSelector(".search-box input[type='text']");
    private final By searchButton = By.xpath("//div[contains(@class,'search-box')]//button[normalize-space()='Search']");
    private final By noResultsMessage = By.xpath("//p[contains(text(),'No villas found')]");
    private final By villaCards = By.cssSelector(".villa-card");
    private final By villaNames = By.cssSelector(".villa-name");
    private final By loadingText = By.xpath("//p[normalize-space()='Loading villas...']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
        waitForVillasToLoad();
    }

    private void waitForVillasToLoad() {
        // "Loading villas..." is replaced by the grid once the fetch resolves.
        wait.until(d -> d.findElements(loadingText).isEmpty());
    }

    public LoginPage clickSignIn() {
        clickable(signInButton).click();
        return new LoginPage(driver);
    }

    public boolean isSignedIn() {
        return isPresent(signOutButton);
    }

    public String getGreetingText() {
        return visible(greeting).getText();
    }

    public void clickSignOut() {
        clickable(signOutButton).click();
    }

    // --- Search ---

    public void searchFor(String term) {
        WebElement input = visible(destinationInput);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        if (!term.isEmpty()) {
            input.sendKeys(term);
        }
        clickable(searchButton).click();
        waitForSearchResults();
    }

    /** Waits until either the "no results" message or at least one villa card appears. */
    private void waitForSearchResults() {
        wait.until(d -> !d.findElements(noResultsMessage).isEmpty() || !d.findElements(villaCards).isEmpty());
    }

    public boolean hasNoResultsMessage() {
        return isPresent(noResultsMessage);
    }

    public int villaCardCount() {
        return driver.findElements(villaCards).size();
    }

    public List<String> getVillaNames() {
        return driver.findElements(villaNames).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Fails fast with a clear message instead of a confusing NoSuchElement
     * error, if the connected database has no villas seeded yet. Also waits
     * for the initial "Loading villas..." fetch to finish first, since this
     * can be called right after arriving at the homepage via a redirect
     * (e.g. after signup) rather than a direct open(), where the fetch may
     * still be in flight.
     */
    public void ensureAtLeastOneVillaExists() {
        waitForVillasToLoad();
        if (villaCardCount() == 0) {
            throw new SkipException(
                    "No villas found on the homepage. Add at least one villa " +
                    "via the Admin page (or seed the database) before running booking tests."
            );
        }
    }

    /** Clicks the first villa card and returns the resulting VillaDetailsPage. */
    public VillaDetailsPage clickFirstVillaCard() {
        ensureAtLeastOneVillaExists();
        List<WebElement> cards = driver.findElements(villaCards);
        wait.until(ExpectedConditions.elementToBeClickable(cards.get(0))).click();
        return new VillaDetailsPage(driver);
    }
}

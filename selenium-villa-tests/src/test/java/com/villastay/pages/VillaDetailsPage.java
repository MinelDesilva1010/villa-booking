package com.villastay.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VillaDetails.jsx has no ids/data-testid. The page also contains a SECOND
 * form later on (the review form) which also has a <select> and inputs, so
 * booking-form locators are scoped relative to the two <input type="date">
 * fields, which only exist inside the booking form. Review-form locators are
 * similarly scoped relative to the <textarea>, which only exists in the
 * review form.
 */
public class VillaDetailsPage extends BasePage {

    private final By villaHeading = By.cssSelector("h1");
    private final By pricePerNight = By.cssSelector(".villa-price");
    private final By loadingText = By.xpath("//p[normalize-space()='Loading villa...']");
    private final By villaNotFound = By.xpath("//p[normalize-space()='Villa not found.']");

    private final By bookThisVillaButton = By.xpath("//button[normalize-space()='Book this villa']");

    // Scoped to the booking form via the date inputs, which are unique to it.
    private final By checkInInput = By.xpath("(//input[@type='date'])[1]");
    private final By checkOutInput = By.xpath("(//input[@type='date'])[2]");
    private final By guestsSelect = By.xpath("//input[@type='date']/ancestor::form//select");
    private final By nameInput = By.xpath("//input[@type='date']/ancestor::form//input[@placeholder='Your name']");
    private final By emailInput = By.xpath("//input[@type='date']/ancestor::form//input[@type='email']");
    private final By submitBookingButton = By.xpath("//input[@type='date']/ancestor::form//button[@type='submit']");
    private final By totalPreview = By.xpath("//input[@type='date']/ancestor::form//p[contains(text(),'night')]");

    private final By confirmationHeading = By.xpath("//h3[contains(text(),'Booking confirmed')]");
    private final By confirmationDetails = By.xpath("//h3[contains(text(),'Booking confirmed')]/following-sibling::p");

    // --- Reviews section ---
    private final By reviewsHeading = By.xpath("//h3[contains(text(),'Guest Reviews')]");
    private final By noReviewsMessage = By.xpath("//p[normalize-space()='No reviews yet — be the first!']");
    private final By signInToReviewLink = By.linkText("Sign in");
    // Scoped to the review form via the textarea, which is unique to it.
    private final By ratingSelect = By.xpath("//textarea/ancestor::form//select");
    private final By commentTextarea = By.cssSelector("textarea");
    private final By submitReviewButton = By.xpath("//textarea/ancestor::form//button[@type='submit']");
    private final By thankYouMessage = By.xpath("//p[contains(text(),'Thanks for your review')]");

    public VillaDetailsPage(WebDriver driver) {
        super(driver);
    }

    private void waitForLoad() {
        wait.until(d -> d.findElements(loadingText).isEmpty());
    }

    public boolean isVillaNotFound() {
        return isPresent(villaNotFound);
    }

    public String getVillaName() {
        waitForLoad();
        return visible(villaHeading).getText();
    }

    /** Parses the "$450 / night" (or "$1,200 / night") text into 450 / 1200. */
    public int getPricePerNight() {
        waitForLoad();
        String text = visible(pricePerNight).getText();
        Matcher m = Pattern.compile("\\$([\\d,]+)").matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1).replace(",", ""));
        }
        throw new IllegalStateException("Could not parse price from: " + text);
    }

    public void clickBookThisVilla() {
        clickable(bookThisVillaButton).click();
        visible(checkInInput);
    }

    public VillaDetailsPage enterCheckIn(String yyyyMmDd) {
        setDateValue(checkInInput, yyyyMmDd);
        return this;
    }

    public VillaDetailsPage enterCheckOut(String yyyyMmDd) {
        setDateValue(checkOutInput, yyyyMmDd);
        return this;
    }

    /** Diagnostic: reads the raw DOM "value" attribute of the check-in input. */
    public String getCheckInDomValue() {
        return visible(checkInInput).getAttribute("value");
    }

    /** Diagnostic: reads the raw DOM "value" attribute of the check-out input. */
    public String getCheckOutDomValue() {
        return visible(checkOutInput).getAttribute("value");
    }

    public VillaDetailsPage selectGuests(String guestsValue) {
        new Select(visible(guestsSelect)).selectByVisibleText(guestsValue);
        return this;
    }

    public VillaDetailsPage enterName(String name) {
        type(nameInput, name);
        return this;
    }

    public VillaDetailsPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public boolean hasTotalPreview() {
        return isPresent(totalPreview);
    }

    public String getTotalPreviewText() {
        return visible(totalPreview).getText();
    }

    public void submitBooking() {
        clickable(submitBookingButton).click();
        visible(confirmationHeading);
    }

    public boolean isBookingConfirmed() {
        return isPresent(confirmationHeading);
    }

    /** Combined text of both confirmation <p> lines (stay details + total). */
    public String getConfirmationText() {
        List<WebElement> paragraphs = driver.findElements(confirmationDetails);
        StringBuilder sb = new StringBuilder();
        for (WebElement p : paragraphs) {
            sb.append(p.getText()).append(" ");
        }
        return sb.toString().trim();
    }

    /** Fills every booking field and submits in one call. */
    public void bookVilla(String checkIn, String checkOut, String guests, String name, String email) {
        clickBookThisVilla();
        enterCheckIn(checkIn);
        enterCheckOut(checkOut);
        selectGuests(guests);
        enterName(name);
        enterEmail(email);
        submitBooking();
    }

    // --- Reviews ---

    /**
     * The "Guest Reviews" heading renders as soon as the villa itself has
     * loaded (regardless of whether the reviews fetch has resolved yet), so
     * waiting for it is a reliable signal that the whole details view —
     * including the sign-in-prompt-vs-form branch — has rendered.
     */
    private void waitForReviewsSectionToLoad() {
        visible(reviewsHeading);
    }

    /** Parses the "N" out of "⭐ Guest Reviews (N)", waiting for the async reviews fetch to settle. */
    public int getReviewCount() {
        return waitForStableCount(() -> {
            String text = visible(reviewsHeading).getText();
            Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(text);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            throw new IllegalStateException("Could not parse review count from: " + text);
        });
    }

    public boolean hasNoReviewsMessage() {
        waitForReviewsSectionToLoad();
        return isPresent(noReviewsMessage);
    }

    /** True when logged out: the "Sign in to leave a review" prompt is shown instead of a form. */
    public boolean hasSignInPrompt() {
        waitForReviewsSectionToLoad();
        return isPresent(signInToReviewLink);
    }

    /** True when logged in and the rating/comment review form is rendered. */
    public boolean isReviewFormPresent() {
        waitForReviewsSectionToLoad();
        return isPresent(commentTextarea);
    }

    public LoginPage clickSignInFromReviews() {
        clickable(signInToReviewLink).click();
        return new LoginPage(driver);
    }

    /** Selects a star rating by its underlying value: "1".."5". */
    public VillaDetailsPage selectRating(String ratingValue) {
        new Select(visible(ratingSelect)).selectByValue(ratingValue);
        return this;
    }

    public String getSelectedRatingValue() {
        return new Select(visible(ratingSelect)).getFirstSelectedOption().getAttribute("value");
    }

    public VillaDetailsPage enterComment(String comment) {
        type(commentTextarea, comment);
        return this;
    }

    /** Submits the review form and waits for the "Thanks for your review" confirmation. */
    public void submitReview() {
        clickable(submitReviewButton).click();
        visible(thankYouMessage);
    }

    /** Submits the review form but expects it NOT to succeed (e.g. missing required field). */
    public void submitReviewExpectingNoChange() {
        clickable(submitReviewButton).click();
    }

    public boolean isReviewSubmitted() {
        return isPresent(thankYouMessage);
    }

    public String getThankYouText() {
        return visible(thankYouMessage).getText();
    }

    /** Checks whether any review card on the page contains the given comment text. */
    public boolean hasReviewWithComment(String commentText) {
        return isPresent(By.xpath("//p[contains(text(), " + xpathLiteral(commentText) + ")]"));
    }

    /** Safely quotes a string for use inside an XPath expression, even if it contains quote characters. */
    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        String[] parts = value.split("'");
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) sb.append(", \"'\", ");
        }
        sb.append(")");
        return sb.toString();
    }
}


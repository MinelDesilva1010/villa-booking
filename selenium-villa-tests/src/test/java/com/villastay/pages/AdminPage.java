package com.villastay.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Admin.jsx has no ids/data-testid, same as the rest of the app. Two
 * important quirks specific to this page:
 *  - Auth is plain React state (not persisted), so every test must log in
 *    fresh via the password gate.
 *  - Deletes go through native window.confirm() dialogs, which Selenium must
 *    handle via the Alert API (see BasePage.acceptConfirmDialog()), not by
 *    clicking anything in the DOM.
 */
public class AdminPage extends BasePage {

    private static final String ADMIN_PATH = "/admin";

    // --- Login gate ---
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By loginSubmitButton = By.cssSelector("button[type='submit']");
    private final By loginError = By.xpath("//p[contains(text(),'Wrong password')]");
    private final By adminPanelHeading = By.xpath("//h2[normalize-space()='Admin Panel']");

    // --- Tabs ---
    private final By villasTabButton = By.xpath("//button[contains(text(),'Villas')]");
    private final By packagesTabButton = By.xpath("//button[contains(text(),'Packages')]");
    private final By bookingsTabButton = By.xpath("//button[contains(text(),'Bookings')]");

    // --- Villas tab ---
    private final By villaNameInput = By.cssSelector("input[placeholder='e.g. Sunset Villa']");
    private final By villaLocationInput = By.cssSelector("input[placeholder='e.g. Galle, Sri Lanka']");
    private final By villaPriceInput = By.cssSelector("input[placeholder='e.g. 350']");
    private final By villaDescInput = By.cssSelector("input[placeholder='Short description...']");
    private final By villaFormSubmitButton =
            By.xpath("//input[@placeholder='e.g. Sunset Villa']/ancestor::form//button[@type='submit']");
    private final By allVillasHeading = By.xpath("//h3[contains(text(),'All Villas')]");

    // --- Packages tab ---
    private final By selectVillaDropdown = By.xpath("//option[text()='-- Select a villa --']/parent::select");
    private final By packageNameInput = By.cssSelector("input[placeholder='e.g. Deluxe Sea View Room']");
    private final By packageDescInput = By.cssSelector("input[placeholder='e.g. 1 large double bed, sea view']");
    private final By packagePriceInput = By.cssSelector("input[placeholder='e.g. 200']");
    private final By packageFormSubmitButton =
            By.xpath("//input[@placeholder='e.g. Deluxe Sea View Room']/ancestor::form//button[@type='submit']");
    private final By packagesHeading = By.xpath("//h3[contains(text(),'Packages (')]");

    // --- Bookings tab ---
    private final By allBookingsHeading = By.xpath("//h3[contains(text(),'All Bookings')]");

    public AdminPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + ADMIN_PATH);
        visible(passwordInput);
    }

    // --- Login ---

    public void login(String password) {
        type(passwordInput, password);
        clickable(loginSubmitButton).click();
    }

    public boolean hasLoginError() {
        return isPresent(loginError);
    }

    public boolean isAdminPanelVisible() {
        return isPresent(adminPanelHeading);
    }

    // --- Tabs ---

    public void switchToVillasTab() {
        clickable(villasTabButton).click();
        visible(allVillasHeading);
    }

    public void switchToPackagesTab() {
        clickable(packagesTabButton).click();
        visible(selectVillaDropdown);
    }

    public void switchToBookingsTab() {
        clickable(bookingsTabButton).click();
        visible(allBookingsHeading);
    }

    // --- Villas CRUD ---

    /**
     * Fills and submits the Add/Edit Villa form. Same form is reused for both.
     * NOTE: on success, Admin.jsx calls fetchVillas() WITHOUT awaiting it,
     * then immediately resets the form fields synchronously. So waiting for
     * the form to clear is not a reliable "done" signal — the list refetch
     * can still be in flight. We wait for the actual expected outcome
     * (the name appearing in the list) instead.
     */
    public void submitVillaForm(String name, String location, String price, String desc) {
        type(villaNameInput, name);
        type(villaLocationInput, location);
        type(villaPriceInput, price);
        type(villaDescInput, desc);
        clickable(villaFormSubmitButton).click();
        wait.until(d -> hasVillaNamed(name));
    }

    public int getVillaCount() {
        return parseCount(visible(allVillasHeading).getText());
    }

    public boolean hasVillaNamed(String name) {
        return isPresent(By.xpath("//div[normalize-space()='" + name + "']"));
    }

    /** Clicks Edit on the villa row matching this name; the form fields populate for editing. */
    public void clickEditVilla(String name) {
        clickable(By.xpath(villaRowXpath(name) + "//button[normalize-space()='Edit']")).click();
        wait.until(d -> !visible(villaNameInput).getAttribute("value").isEmpty());
    }

    /** Clicks Delete on the villa row matching this name and accepts the confirm dialog. */
    public void clickDeleteVilla(String name) {
        clickable(By.xpath(villaRowXpath(name) + "//button[normalize-space()='Delete']")).click();
        acceptConfirmDialog();
        wait.until(d -> !hasVillaNamed(name));
    }

    private String villaRowXpath(String name) {
        return "//div[normalize-space()='" + name + "']/ancestor::div[2]";
    }

    // --- Packages CRUD ---

    public void selectVillaForPackages(String villaName) {
        new Select(visible(selectVillaDropdown)).selectByVisibleText(villaName);
        visible(packageNameInput);
    }

    /**
     * Same race condition as submitVillaForm(): handlePackageSubmit() calls
     * fetchPackages() without awaiting it, so we wait for the actual outcome
     * (the package name appearing in the list) rather than the form clearing.
     */
    public void submitPackageForm(String name, String desc, String price) {
        type(packageNameInput, name);
        type(packageDescInput, desc);
        type(packagePriceInput, price);
        clickable(packageFormSubmitButton).click();
        wait.until(d -> hasPackageNamed(name));
    }

    public int getPackageCount() {
        return parseCount(visible(packagesHeading).getText());
    }

    public boolean hasPackageNamed(String name) {
        return isPresent(By.xpath("//div[normalize-space()='" + name + "']"));
    }

    public void clickDeletePackage(String name) {
        String rowXpath = "//div[normalize-space()='" + name + "']/ancestor::div[2]";
        clickable(By.xpath(rowXpath + "//button[normalize-space()='Delete']")).click();
        acceptConfirmDialog();
        wait.until(d -> !hasPackageNamed(name));
    }

    // --- Bookings ---

    public int getBookingCount() {
        return parseCount(visible(allBookingsHeading).getText());
    }

    // --- shared ---

    /** Parses the "N" out of headings like "All Villas (N)" / "Packages (N)". */
    private int parseCount(String headingText) {
        Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(headingText);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalStateException("Could not parse count from: " + headingText);
    }
}

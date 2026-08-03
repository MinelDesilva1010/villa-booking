package com.villastay.pages;

import com.villastay.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.timeoutSeconds()));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void type(By locator, String text) {
        WebElement el = visible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /**
     * sendKeys() to <input type="date"> is unreliable: Chrome expects
     * keystrokes in the locale's on-screen segment order (e.g. month/day/year
     * digits for en-US), not an ISO "yyyy-MM-dd" string with dashes. This sets
     * the DOM value directly (which is always ISO format per the HTML5 spec,
     * regardless of display locale) and fires a real "input" event through
     * React's native value setter override, so React's onChange/state update
     * fires exactly as if the user had picked that date.
     */
    protected void setDateValue(By locator, String isoDate) {
        WebElement el = visible(locator);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const el = arguments[0];" +
                        "const value = arguments[1];" +
                        "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "setter.call(el, value);" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));",
                el, isoDate
        );
    }

    /**
     * Admin.jsx uses native window.confirm() before deleting a villa/package.
     * Selenium must handle this via the Alert API, not by clicking a DOM
     * element — it's a real browser-level dialog, not part of the page.
     */
    protected void acceptConfirmDialog() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }
}

package com.villastay.tests;

import com.villastay.base.BaseTest;
import com.villastay.pages.HomePage;
import com.villastay.pages.VillaDetailsPage;
import com.villastay.utils.ConfigReader;
import com.villastay.utils.DateUtils;
import com.villastay.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers Home.jsx search + VillaDetails.jsx booking form.
 *
 * IMPORTANT PREREQUISITE: VillaDetails.jsx fetches villa/package/review data
 * from http://localhost:5000 (not the deployed Render URL like the rest of
 * the app). These tests require your backend running LOCALLY on port 5000
 * at the same time as the frontend (npm run dev). If the connected database
 * has zero villas seeded, tests that need a villa card will be SKIPPED with
 * a clear message rather than failing confusingly.
 */
public class BookingFlowTest extends BaseTest {

    @Test(description = "Searching for a destination with no matches shows the empty state")
    public void testSearchWithNoMatchesShowsEmptyState() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());

        homePage.searchFor("zzzNoSuchDestinationzzz");

        Assert.assertTrue(homePage.hasNoResultsMessage(),
                "Expected 'No villas found' message for a nonsense search term");
        Assert.assertEquals(homePage.villaCardCount(), 0,
                "Expected zero villa cards for a nonsense search term");
    }

    @Test(description = "Clearing the search restores the full villa list")
    public void testClearingSearchRestoresAllVillas() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());

        homePage.searchFor("zzzNoSuchDestinationzzz");
        Assert.assertTrue(homePage.hasNoResultsMessage());

        homePage.searchFor(""); // clear and re-search

        homePage.ensureAtLeastOneVillaExists();
        Assert.assertTrue(homePage.villaCardCount() > 0,
                "Expected villa cards to reappear after clearing the search");
    }

    @Test(description = "Clicking a villa card opens its details page with matching name")
    public void testClickingVillaCardOpensDetailsPage() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());
        homePage.ensureAtLeastOneVillaExists();

        String expectedName = homePage.getVillaNames().get(0);
        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();

        Assert.assertTrue(driver.getCurrentUrl().contains("/villa/"),
                "Expected URL to navigate to a /villa/:id route");
        Assert.assertEquals(detailsPage.getVillaName(), expectedName,
                "Expected details page heading to match the clicked villa's name");
    }

    @Test(description = "The live total preview updates correctly as dates are filled in. " +
            "Under active investigation — diagnostic assertions added to pinpoint the exact failure point.")
    public void testBookingTotalPreviewCalculatesCorrectly() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());
        homePage.ensureAtLeastOneVillaExists();

        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();
        int pricePerNight = detailsPage.getPricePerNight();

        String checkIn = DateUtils.futureDate(10);
        String checkOut = DateUtils.futureDate(13); // 3 nights

        detailsPage.clickBookThisVilla();
        detailsPage.enterCheckIn(checkIn);
        detailsPage.enterCheckOut(checkOut);

        // Diagnostic checkpoint: confirm the dates actually landed before
        // checking the derived total. If this fails, the JS date-setting
        // trick didn't stick. If this PASSES but the assertion below still
        // fails, the bug is in the nights/total calculation itself, not in
        // how we're entering dates.
        Assert.assertEquals(detailsPage.getCheckInDomValue(), checkIn,
                "Check-in input's DOM value doesn't match what we tried to set");
        Assert.assertEquals(detailsPage.getCheckOutDomValue(), checkOut,
                "Check-out input's DOM value doesn't match what we tried to set");

        String previewText = detailsPage.getTotalPreviewText(); // waits for it to render
        String expectedTotal = String.valueOf(3 * pricePerNight);
        Assert.assertTrue(
                previewText.contains(expectedTotal),
                "Expected preview to show 3 nights x $" + pricePerNight + " = $" + expectedTotal +
                        ", got: " + previewText
        );
    }

    @Test(description = "A guest can complete a full booking without being logged in")
    public void testCompleteBookingFlowHappyPath() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());
        homePage.ensureAtLeastOneVillaExists();

        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();
        String villaName = detailsPage.getVillaName();

        String checkIn = DateUtils.futureDate(20);
        String checkOut = DateUtils.futureDate(23); // 3 nights
        String guestName = TestDataGenerator.uniqueName();
        String guestEmail = TestDataGenerator.uniqueEmail();

        detailsPage.bookVilla(checkIn, checkOut, "4", guestName, guestEmail);

        Assert.assertTrue(detailsPage.isBookingConfirmed(), "Expected booking confirmation to appear");
        String confirmationText = detailsPage.getConfirmationText();
        Assert.assertTrue(confirmationText.contains(guestName),
                "Expected confirmation to mention the guest's name");
        Assert.assertTrue(confirmationText.contains(villaName),
                "Expected confirmation to mention the villa name");
        Assert.assertTrue(confirmationText.contains(checkIn) && confirmationText.contains(checkOut),
                "Expected confirmation to mention the chosen dates");
    }
}

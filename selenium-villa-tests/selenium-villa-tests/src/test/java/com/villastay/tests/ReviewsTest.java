package com.villastay.tests;

import com.villastay.base.BaseTest;
import com.villastay.pages.HomePage;
import com.villastay.pages.LoginPage;
import com.villastay.pages.VillaDetailsPage;
import com.villastay.utils.ConfigReader;
import com.villastay.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers the reviews section of VillaDetails.jsx:
 *  - Logged-out guests see a "Sign in to leave a review" prompt, no form.
 *  - Logged-in users get a rating + comment form and can submit a review.
 *
 * Each test signs up a brand-new user where login is needed, same reasoning
 * as LoginSignupTest: there's no test/staging environment, so a fresh random
 * account avoids any collision with existing data.
 */
public class ReviewsTest extends BaseTest {

    private static final String PASSWORD = "TestPass123!";

    @Test(description = "A logged-out guest sees a sign-in prompt instead of the review form")
    public void testLoggedOutGuestSeesSignInPromptNotForm() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());
        homePage.ensureAtLeastOneVillaExists();

        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();

        Assert.assertTrue(detailsPage.hasSignInPrompt(),
                "Expected a 'Sign in to leave a review' prompt for a logged-out guest");
        Assert.assertFalse(detailsPage.isReviewFormPresent(),
                "Review form should NOT render for a logged-out guest");
    }

    @Test(description = "Clicking the reviews 'Sign in' prompt navigates to the login page")
    public void testSignInPromptLinksToLoginPage() {
        HomePage homePage = new HomePage(driver);
        homePage.open(ConfigReader.baseUrl());
        homePage.ensureAtLeastOneVillaExists();

        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();
        LoginPage loginPage = detailsPage.clickSignInFromReviews();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Expected navigating to the login page");
        Assert.assertEquals(loginPage.getHeadingText(), "Sign in");
    }

    @Test(description = "The rating field defaults to 5 stars for a logged-in user")
    public void testReviewFormDefaultsToFiveStars() {
        HomePage homePage = signUpFreshUserAndLandOnHome();
        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();

        Assert.assertTrue(detailsPage.isReviewFormPresent(), "Expected review form for a logged-in user");
        Assert.assertEquals(detailsPage.getSelectedRatingValue(), "5",
                "Expected the rating select to default to 5 stars");
    }

    @Test(description = "A logged-in user can submit a review and see it reflected on the page")
    public void testLoggedInUserCanSubmitReview() {
        String guestName = TestDataGenerator.uniqueName();
        HomePage homePage = signUpFreshUserAndLandOnHomeAs(guestName);
        VillaDetailsPage detailsPage = homePage.clickFirstVillaCard();

        int countBefore = detailsPage.getReviewCount();
        String comment = "Automated test review " + TestDataGenerator.uniqueEmail();

        detailsPage.selectRating("4");
        detailsPage.enterComment(comment);
        detailsPage.submitReview();

        Assert.assertTrue(detailsPage.isReviewSubmitted(), "Expected the thank-you confirmation to appear");
        Assert.assertTrue(detailsPage.getThankYouText().contains(guestName),
                "Expected thank-you message to address the reviewer by name");
        Assert.assertEquals(detailsPage.getReviewCount(), countBefore + 1,
                "Expected review count to increase by exactly 1");
        Assert.assertTrue(detailsPage.hasReviewWithComment(comment),
                "Expected the submitted comment to appear in the reviews list");
    }

    // --- helpers ---

    private HomePage signUpFreshUserAndLandOnHome() {
        return signUpFreshUserAndLandOnHomeAs(TestDataGenerator.uniqueName());
    }

    private HomePage signUpFreshUserAndLandOnHomeAs(String name) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(ConfigReader.baseUrl());
        HomePage homePage = loginPage.signUp(name, TestDataGenerator.uniqueEmail(), PASSWORD);
        homePage.ensureAtLeastOneVillaExists();
        return homePage;
    }
}

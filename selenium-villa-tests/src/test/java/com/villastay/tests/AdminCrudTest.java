package com.villastay.tests;

import com.villastay.base.BaseTest;
import com.villastay.pages.AdminPage;
import com.villastay.utils.ConfigReader;
import com.villastay.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers Admin.jsx: the password gate, and full add/edit/delete lifecycles
 * for villas and packages against the real live database (no staging env,
 * same reasoning as the other suites). Each CRUD test creates its own
 * villa/package with a unique name and deletes it again at the end, so it
 * doesn't leave permanent clutter behind even though there's no cleanup API
 * beyond what the UI itself offers.
 */
public class AdminCrudTest extends BaseTest {

    @Test(description = "Wrong admin password shows an error and does not unlock the panel")
    public void testWrongPasswordShowsError() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.open(ConfigReader.baseUrl());

        adminPage.login("definitely-not-the-password");

        Assert.assertTrue(adminPage.hasLoginError(), "Expected a 'Wrong password!' error");
        Assert.assertFalse(adminPage.isAdminPanelVisible(), "Admin panel should stay locked");
    }

    @Test(description = "Correct admin password unlocks the panel")
    public void testCorrectPasswordUnlocksPanel() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.open(ConfigReader.baseUrl());

        adminPage.login(ConfigReader.adminPassword());

        Assert.assertTrue(adminPage.isAdminPanelVisible(), "Expected the Admin Panel heading after correct login");
    }

    @Test(description = "A villa can be added, edited, and deleted end-to-end")
    public void testAddEditDeleteVillaLifecycle() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.open(ConfigReader.baseUrl());
        adminPage.login(ConfigReader.adminPassword());
        adminPage.switchToVillasTab();

        String villaName = "Selenium Test Villa " + TestDataGenerator.uniqueEmail();
        int countBefore = adminPage.getVillaCount();

        // Create
        adminPage.submitVillaForm(villaName, "Test Bay, Sri Lanka", "199", "A villa created by an automated test.");
        Assert.assertTrue(adminPage.hasVillaNamed(villaName), "Expected new villa to appear in the list");
        Assert.assertEquals(adminPage.getVillaCount(), countBefore + 1);

        // Edit (change name to confirm the update actually took effect)
        String updatedName = villaName + " (edited)";
        adminPage.clickEditVilla(villaName);
        adminPage.submitVillaForm(updatedName, "Test Bay, Sri Lanka", "249", "Updated by an automated test.");
        Assert.assertTrue(adminPage.hasVillaNamed(updatedName), "Expected updated villa name to appear");
        Assert.assertFalse(adminPage.hasVillaNamed(villaName), "Old villa name should no longer appear after edit");
        Assert.assertEquals(adminPage.getVillaCount(), countBefore + 1, "Editing should not change the total count");

        // Delete (cleanup)
        adminPage.clickDeleteVilla(updatedName);
        Assert.assertFalse(adminPage.hasVillaNamed(updatedName), "Villa should be gone after delete");
        Assert.assertEquals(adminPage.getVillaCount(), countBefore, "Count should return to its original value");
    }

    @Test(description = "A package can be added and deleted for a freshly created villa")
    public void testAddDeletePackageLifecycle() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.open(ConfigReader.baseUrl());
        adminPage.login(ConfigReader.adminPassword());
        adminPage.switchToVillasTab();

        // Create a throwaway villa to attach the package to.
        String villaName = "Selenium Package Host " + TestDataGenerator.uniqueEmail();
        adminPage.submitVillaForm(villaName, "Test Bay, Sri Lanka", "150", "Villa used to test package CRUD.");
        Assert.assertTrue(adminPage.hasVillaNamed(villaName));

        adminPage.switchToPackagesTab();
        adminPage.selectVillaForPackages(villaName);
        int countBefore = adminPage.getPackageCount();

        String packageName = "Selenium Test Package " + TestDataGenerator.uniqueEmail();
        adminPage.submitPackageForm(packageName, "A package created by an automated test.", "120");

        Assert.assertTrue(adminPage.hasPackageNamed(packageName), "Expected new package to appear in the list");
        Assert.assertEquals(adminPage.getPackageCount(), countBefore + 1);

        // Cleanup: delete the package, then the villa.
        adminPage.clickDeletePackage(packageName);
        Assert.assertFalse(adminPage.hasPackageNamed(packageName), "Package should be gone after delete");

        adminPage.switchToVillasTab();
        adminPage.clickDeleteVilla(villaName);
        Assert.assertFalse(adminPage.hasVillaNamed(villaName), "Host villa should be gone after cleanup");
    }

    @Test(description = "The bookings tab renders without error and shows a count")
    public void testBookingsTabRenders() {
        AdminPage adminPage = new AdminPage(driver);
        adminPage.open(ConfigReader.baseUrl());
        adminPage.login(ConfigReader.adminPassword());

        adminPage.switchToBookingsTab();

        // Just a smoke check: the tab renders and produces a non-negative count.
        // Deeper booking-content assertions belong in BookingFlowTest, which
        // controls the exact booking data being asserted on.
        Assert.assertTrue(adminPage.getBookingCount() >= 0, "Expected a valid (non-negative) booking count");
    }
}

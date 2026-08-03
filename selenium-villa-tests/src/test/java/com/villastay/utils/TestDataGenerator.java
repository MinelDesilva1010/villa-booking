package com.villastay.utils;

import java.util.UUID;

/**
 * CeylonVillas has no test/staging environment or a "delete user" endpoint,
 * and signup hits the real deployed backend. To avoid "Email already
 * registered" failures on repeated test runs, every signup test must use
 * a fresh, unique email.
 */
public class TestDataGenerator {

    public static String uniqueEmail() {
        String uniquePart = UUID.randomUUID().toString().substring(0, 8);
        return "selenium.test." + uniquePart + "@example.com";
    }

    public static String uniqueName() {
        String uniquePart = UUID.randomUUID().toString().substring(0, 4);
        return "Test User " + uniquePart;
    }
}

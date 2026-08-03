package com.villastay.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config.properties once and exposes typed getters.
 *
 * Any value can be overridden at runtime via a matching -D system property,
 * e.g. `mvn test -Dheadless=true` overrides headless without editing the
 * checked-in config.properties. This is how CI runs headless while local
 * runs stay visible by default.
 */
public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    /** Checks a -D system property first, falling back to config.properties. */
    private static String resolve(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return props.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return resolve(key, null);
    }

    public static String baseUrl() {
        return resolve("base.url", null);
    }

    public static String browser() {
        return resolve("browser", "chrome");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(resolve("headless", "false"));
    }

    public static int timeoutSeconds() {
        return Integer.parseInt(resolve("timeout.seconds", "10"));
    }

    public static String adminPassword() {
        return resolve("admin.password", null);
    }
}

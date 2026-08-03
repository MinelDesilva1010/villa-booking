package com.villastay.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter HTML_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    /** Returns today + daysFromNow, formatted for an <input type="date"> field. */
    public static String futureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow).format(HTML_DATE_FORMAT);
    }
}

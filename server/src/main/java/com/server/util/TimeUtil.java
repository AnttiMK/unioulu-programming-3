package com.server.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for time related operations.
 */
public final class TimeUtil {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private TimeUtil() {
        // Static utility class
    }

    /**
     * Converts a date string with a predefined format to epoch milliseconds.
     *
     * @param date The date string
     * @return The epoch milliseconds
     * @throws DateTimeException If the date string is invalid or is not in the correct format
     */
    public static long dateStringToEpochMilli(String date) throws DateTimeException {
        return ZonedDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN)).toInstant().toEpochMilli();
    }

    /**
     * Converts epoch milliseconds to a date string with a predefined format.
     *
     * @param epochMs The epoch milliseconds
     * @return The date string
     */
    public static String epochMilliToDateString(long epochMs) {
        return Instant.ofEpochMilli(epochMs).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

}

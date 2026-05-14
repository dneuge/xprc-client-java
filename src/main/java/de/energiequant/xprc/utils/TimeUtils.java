package de.energiequant.xprc.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtils {
    private TimeUtils() {
        // utility class; hide constructor
    }

    public static Instant parseOffsetTimestamp(String s) {
        try {
            // workaround for Java 8 where Instant.parse and ISO_INSTANT formatter fail to parse numeric offsets
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(s, Instant::from);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Timestamp could not be parsed (expected ISO 8601 date and time with offset): \"" + s + "\"", ex);
        }
    }
}

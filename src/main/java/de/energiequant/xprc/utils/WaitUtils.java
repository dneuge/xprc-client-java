package de.energiequant.xprc.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(WaitUtils.class);

    private WaitUtils() {
        // utility class; hide constructor
    }

    public static boolean sleepFor(Duration sleepDuration, Duration checkInterval, BooleanSupplier breakCondition, String logPrefix) {
        long checkIntervalMillis = checkInterval.toMillis();

        Instant endOfSleep = Instant.now().plus(sleepDuration);
        long millisUntilComplete = Duration.between(Instant.now(), endOfSleep).toMillis();
        while ((millisUntilComplete > 0) && !breakCondition.getAsBoolean()) {
            try {
                Thread.sleep(Math.min(millisUntilComplete, checkIntervalMillis));
            } catch (InterruptedException ex) {
                LOGGER.warn("{}Sleep interrupted", logPrefix, ex);
                return false;
            }

            millisUntilComplete = Duration.between(Instant.now(), endOfSleep).toMillis();
        }

        return millisUntilComplete <= 0;
    }
}

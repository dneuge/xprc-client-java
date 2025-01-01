package de.energiequant.xprc;

import java.time.Duration;

public class Interval {
    // FIXME: draft/WIP

    private final long value;
    private final boolean isMillis;

    private Interval(long value, boolean isMillis) {
        this.value = value;
        this.isMillis = isMillis;
    }

    public static Interval of(Duration duration) {
        return ofMillis(duration.toMillis());
    }

    public static Interval ofMillis(long milliseconds) {
        return new Interval(milliseconds, true);
    }

    public static Interval ofFrames(int numFrames) {
        return new Interval(numFrames, false);
    }
}

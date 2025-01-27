package de.energiequant.xprc;

import java.time.Duration;

public class Interval {
    // FIXME: draft/WIP

    public static Interval ONCE = new Interval(0, false);

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
        if (milliseconds < 1) {
            throw new IllegalArgumentException("Time-based intervals must be 1ms or greater, got " + milliseconds);
        }

        return new Interval(milliseconds, true);
    }

    public static Interval ofFrames(int numFrames) {
        if (numFrames < 1) {
            throw new IllegalArgumentException("Frame-based intervals must span at least 1 frame, got " + numFrames);
        }

        return new Interval(numFrames, false);
    }

    public String encode() {
        if (value == 0) {
            return "0";
        }

        return value + ((isMillis) ? "ms" : "f");
    }

    public boolean isSpecialMode() {
        return value <= 0;
    }
}

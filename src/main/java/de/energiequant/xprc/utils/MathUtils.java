package de.energiequant.xprc.utils;

public class MathUtils {
    private MathUtils() {
        // utility class; hide constructor
    }

    /**
     * Calculates the exact integer-based power. Callers must ensure that the result remains in integer range, meaning
     * this method should only be used to process known constants at this time.
     *
     * @param base
     * @param exponent
     * @return
     */
    public static int intPow(int base, int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Unsupported exponent: " + exponent);
        }

        int res = 1;
        for (int i = 1; i <= exponent; i++) {
            res *= base;
        }

        return res;
    }
}

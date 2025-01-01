package de.energiequant.xprc;

import de.energiequant.xprc.utils.MathUtils;

/**
 * An immutable XPRC channel ID available both in alphanumeric protocol format and client-internal numeric
 * representation.
 *
 * <p>
 * Numeric representation is not specified in XPRC protocol (would only be specified for a not yet needed binary
 * format), it is mainly used for channel allocation and should not be exposed to users. Until officially specified,
 * conversion between numeric and alphanumeric representations is not fixed to any specific method. The only
 * requirements are that both representations must be uniquely translated to each other and the numeric representation
 * should only use positive numbers (incl. zero).
 * </p>
 */
public class ChannelId implements Comparable<ChannelId> {
    private final int numeric;
    private final String alphanumeric;

    private static final int ALPHA_NUMERIC_LENGTH = 4;

    private static final int LOWER_BOUND_NUMERIC = 0;
    private static final int UPPER_BOUND_NUMERIC = 9;
    private static final int LOWER_BOUND_UPPERCASE = UPPER_BOUND_NUMERIC + 1;
    private static final int UPPER_BOUND_UPPERCASE = LOWER_BOUND_UPPERCASE + 26 - 1;
    private static final int LOWER_BOUND_LOWERCASE = UPPER_BOUND_UPPERCASE + 1;
    private static final int UPPER_BOUND_LOWERCASE = LOWER_BOUND_LOWERCASE + 26 - 1;
    private static final int NUM_CHARACTERS = UPPER_BOUND_LOWERCASE + 1;

    public static final int NUM_POSSIBLE_CHANNELS = MathUtils.intPow(NUM_CHARACTERS, ALPHA_NUMERIC_LENGTH);

    public static final int FIRST_NUMERIC = 0;

    private ChannelId(int numeric, String alphanumeric) {
        this.numeric = numeric;
        this.alphanumeric = alphanumeric;
    }

    /**
     * Returns a {@link ChannelId} object for the channel specified in client-internal numeric representation.
     *
     * @param numeric client-specific internal representation (positive number incl. zero)
     * @return object describing the channel
     */
    public static ChannelId fromNumeric(int numeric) {
        return new ChannelId(numeric, toAlphanumeric(numeric));
    }

    /**
     * Returns a {@link ChannelId} object for the channel specified in alphanumeric XPRC protocol format.
     *
     * @param alphanumeric protocol-formatted alphanumeric channel ID
     * @return object describing the channel
     */
    public static ChannelId fromAlphanumeric(String alphanumeric) {
        return new ChannelId(toNumeric(alphanumeric), alphanumeric);
    }

    /**
     * Converts the given client-specific internal numeric representation to alphanumeric XPRC protocol format.
     *
     * @param numeric client-specific internal numeric representation of a channel ID (positive number incl. zero)
     * @return alphanumeric format to use in XPRC protocol
     */
    public static String toAlphanumeric(int numeric) {
        char[] res = new char[ALPHA_NUMERIC_LENGTH];
        for (int i = res.length - 1; i >= 0; i--) {
            int index = numeric % NUM_CHARACTERS;
            if (index <= UPPER_BOUND_NUMERIC) {
                res[i] = (char) ('0' + (index - LOWER_BOUND_NUMERIC));
            } else if (index <= UPPER_BOUND_UPPERCASE) {
                res[i] = (char) ('A' + (index - LOWER_BOUND_UPPERCASE));
            } else if (index <= UPPER_BOUND_LOWERCASE) {
                res[i] = (char) ('a' + (index - LOWER_BOUND_LOWERCASE));
            } else {
                throw new IllegalArgumentException("Invalid index " + index);
            }

            numeric = numeric / NUM_CHARACTERS;
        }

        return new String(res);
    }

    /**
     * Converts the given alphanumeric formatted channel ID (according to protocol) to a numeric representation
     * specific to this client implementation.
     *
     * @param alphanumeric protocol-formatted alphanumeric channel ID
     * @return client-specific internal numeric representation (positive number incl. zero)
     */
    public static int toNumeric(String alphanumeric) {
        if (alphanumeric == null) {
            throw new IllegalArgumentException("Channel ID must not be null");
        }

        if (alphanumeric.length() != ALPHA_NUMERIC_LENGTH) {
            throw new IllegalArgumentException("Channel IDs must have exactly " + ALPHA_NUMERIC_LENGTH + " characters, got " + alphanumeric.length() + ": \"" + alphanumeric + "\"");
        }

        int res = 0;
        for (char ch : alphanumeric.toCharArray()) {
            res *= NUM_CHARACTERS;
            res += toNumeric(ch);
        }

        return res;
    }

    private static int toNumeric(char ch) {
        if (ch >= '0' && ch <= '9') {
            return LOWER_BOUND_NUMERIC + ch - '0';
        } else if (ch >= 'A' && ch <= 'Z') {
            return LOWER_BOUND_UPPERCASE + ch - 'A';
        } else if (ch >= 'a' && ch <= 'z') {
            return LOWER_BOUND_LOWERCASE + ch - 'a';
        }

        throw new IllegalArgumentException(String.format(
            "Invalid character not supported by XPRC protocol: '%c' (%d, 0x%02X)", ch, (int) ch, (int) ch));
    }

    /**
     * Returns a numeric representation specific to this client implementation.
     *
     * @return client-internal numeric representation (positive number incl. zero)
     */
    public int getNumeric() {
        return numeric;
    }

    /**
     * Returns the alphanumeric representation as used in XPRC protocol.
     *
     * @return alphanumeric representation (XPRC protocol)
     */
    public String getAlphanumeric() {
        return alphanumeric;
    }

    public static int incrementNumeric(int numeric) {
        return (numeric + 1) % NUM_POSSIBLE_CHANNELS;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChannelId)) {
            return false;
        }

        // instances are uniquely identified by numeric value
        ChannelId other = (ChannelId) obj;
        return this.numeric == other.numeric;
    }

    @Override
    public int hashCode() {
        // instances are uniquely identified by numeric value
        return numeric;
    }

    @Override
    public int compareTo(ChannelId o) {
        return Integer.compare(this.numeric, o.numeric);
    }

    @Override
    public String toString() {
        return alphanumeric;
    }
}

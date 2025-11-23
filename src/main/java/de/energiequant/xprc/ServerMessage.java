package de.energiequant.xprc;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerMessage extends ReceivedMessage {
    private final Type type;
    private final int relativeTimestampMillis;
    private final int rawPayloadOffset;

    private static final String GLOBAL_ADDRESS_PREFIX_STRING = String.valueOf(ReceivedMessage.GLOBAL_ADDRESS_PREFIX);
    private static final String ERROR_PREFIX = GLOBAL_ADDRESS_PREFIX_STRING + "ERR ";
    private static final String ACKNOWLEDGE_PREFIX = GLOBAL_ADDRESS_PREFIX_STRING + "ACK "; // not used by server as of January 2025 but possible
    private static final int TIMESTAMP_START_OFFSET = 5; // after "*ERR "/"*ACK "

    private static final Pattern CHANNEL_TERMINATION_IGNORED_PATTERN = Pattern.compile("^termination request ignored, channel does not exist: ([a-zA-Z0-9]{4})$");
    private static final int CHANNEL_TERMINATION_IGNORED_ID = 1;

    public ServerMessage(Instant localReceiveTimestamp, String raw) {
        super(localReceiveTimestamp, raw);

        if (raw.startsWith(ERROR_PREFIX)) {
            this.type = Type.ERROR;
        } else if (raw.startsWith(ACKNOWLEDGE_PREFIX)) {
            this.type = Type.FINALIZATION;
        } else {
            throw new IllegalArgumentException("Unhandled message prefix: \"" + raw + "\"");
        }

        int timestampEndOffset = raw.indexOf(' ', TIMESTAMP_START_OFFSET);
        if (timestampEndOffset < 0) {
            timestampEndOffset = raw.length();
        }
        this.relativeTimestampMillis = Integer.parseUnsignedInt(raw.substring(TIMESTAMP_START_OFFSET, timestampEndOffset));

        this.rawPayloadOffset = (timestampEndOffset < raw.length() - 1) ? timestampEndOffset + 1 : -1;
    }

    /**
     * Tries to parse {@link #CHANNEL_TERMINATION_IGNORED_PATTERN}, indicating a server-side ignored termination request.
     * Returns the {@link ChannelId} termination had been requested for or empty if the message did not match.
     *
     * @return {@link ChannelId} termination had been requested for; empty if different message
     */
    public Optional<ChannelId> tryParseChannelTerminationIgnored() {
        String rawPayload = getRawPayload().orElse(null);
        if (rawPayload == null) {
            return Optional.empty();
        }

        Matcher matcher = CHANNEL_TERMINATION_IGNORED_PATTERN.matcher(rawPayload);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(ChannelId.fromAlphanumeric(matcher.group(CHANNEL_TERMINATION_IGNORED_ID)));
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getRelativeTimestampMillis() {
        return relativeTimestampMillis;
    }

    @Override
    public Optional<String> getRawPayload() {
        if (rawPayloadOffset < 0) {
            return Optional.empty();
        }

        return Optional.of(getRaw().substring(rawPayloadOffset));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "(" + type
            + ", " + relativeTimestampMillis
            + ", raw=\"" + getRaw()
            + "\", localReceiveTimestamp=" + getLocalReceiveTimestamp()
            + ")";
    }
}

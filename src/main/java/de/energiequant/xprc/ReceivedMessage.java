package de.energiequant.xprc;

import java.time.Instant;
import java.util.Optional;

public abstract class ReceivedMessage {
    public enum Type {
        ACKNOWLEDGEMENT, CONTINUATION, FINALIZATION, ERROR;

        public boolean opensChannel() {
            return this == ACKNOWLEDGEMENT;
        }

        public boolean closesChannel() {
            return this == FINALIZATION || this == ERROR;
        }
    }

    private final Instant localReceiveTimestamp;
    private final String raw;

    protected static final char GLOBAL_ADDRESS_PREFIX = '*';

    protected ReceivedMessage(ReceivedMessage other) {
        this.localReceiveTimestamp = other.localReceiveTimestamp;
        this.raw = other.raw;
    }

    protected ReceivedMessage(Instant localReceiveTimestamp, String raw) {
        this.localReceiveTimestamp = localReceiveTimestamp;
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public Instant getLocalReceiveTimestamp() {
        return localReceiveTimestamp;
    }

    public Instant getTimestamp(Instant referenceTimestamp) {
        return referenceTimestamp.plusMillis(getRelativeTimestampMillis());
    }

    public static boolean isGloballyAddressed(String raw) {
        return !raw.isEmpty() && (raw.charAt(0) == GLOBAL_ADDRESS_PREFIX);
    }

    public abstract Type getType();

    public abstract int getRelativeTimestampMillis();

    public abstract Optional<String> getRawPayload();
}

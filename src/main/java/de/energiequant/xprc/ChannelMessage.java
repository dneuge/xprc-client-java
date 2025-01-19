package de.energiequant.xprc;

import java.time.Instant;
import java.util.Optional;

public class ChannelMessage extends ReceivedMessage {
    private final Type type;
    private final ChannelId channelId;
    private final int relativeTimestampMillis;
    private final int rawPayloadOffset;

    /**
     * Copies all fields from given base {@link ChannelMessage} for extension.
     *
     * @param baseMessage message to copy from
     */
    protected ChannelMessage(ChannelMessage baseMessage) {
        super(baseMessage);
        this.type = baseMessage.type;
        this.channelId = baseMessage.channelId;
        this.relativeTimestampMillis = baseMessage.relativeTimestampMillis;
        this.rawPayloadOffset = baseMessage.rawPayloadOffset;
    }

    public ChannelMessage(Instant localReceiveTimestamp, String raw) {
        super(localReceiveTimestamp, raw);

        int channelIdOffset;
        if (raw.startsWith("+ACK ")) {
            this.type = Type.ACKNOWLEDGEMENT;
            channelIdOffset = 5;
        } else if (raw.startsWith("-ACK ")) {
            this.type = Type.FINALIZATION;
            channelIdOffset = 5;
        } else if (raw.startsWith("-ERR ")) {
            this.type = Type.ERROR;
            channelIdOffset = 5;
        } else if (raw.startsWith("+")) {
            this.type = Type.CONTINUATION;
            channelIdOffset = 1;
        } else if (raw.startsWith("-")) {
            this.type = Type.FINALIZATION;
            channelIdOffset = 1;
        } else {
            throw new IllegalArgumentException("Invalid start of message: \"" + raw + "\"");
        }

        this.channelId = ChannelId.fromAlphanumeric(raw.substring(channelIdOffset, channelIdOffset + 4));

        int timestampStartOffset = channelIdOffset + 5;
        int timestampEndOffset = raw.indexOf(' ', timestampStartOffset);
        if (timestampEndOffset < 0) {
            timestampEndOffset = raw.length();
        }
        this.relativeTimestampMillis = Integer.parseUnsignedInt(raw.substring(timestampStartOffset, timestampEndOffset));

        this.rawPayloadOffset = (timestampEndOffset < (raw.length() - 1)) ? timestampEndOffset + 1 : -1;
    }

    @Override
    public Type getType() {
        return type;
    }

    public ChannelId getChannelId() {
        return channelId;
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

    /**
     * Indicates if the {@link ChannelMessage} holds raw payload.
     * <p>
     * Command-specific implementations should not override this method, see {@link #containsData()} instead.
     * </p>
     *
     * @return {@code true} if raw payload is present, {@code false} if not
     * @see #containsData()
     */
    public boolean hasPayload() {
        return rawPayloadOffset >= 0;
    }

    /**
     * Indicates if the command-specific decoded message contains data.
     *
     * @return {@code true} if data is present, {@code false} if not
     */
    public boolean containsData() {
        return type != Type.ERROR && hasPayload();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());

        sb.append("(");
        sb.append(type);
        sb.append(", ");
        sb.append(channelId);
        sb.append(", ");
        sb.append(relativeTimestampMillis);

        toString(sb);

        sb.append(", raw=\"");
        sb.append(getRaw());
        sb.append("\", localReceiveTimestamp=");
        sb.append(getLocalReceiveTimestamp());
        sb.append(")");

        return sb.toString();
    }

    protected void toString(StringBuilder sb) {
        // extension hook; nothing to do for base class
    }
}

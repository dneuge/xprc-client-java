package de.energiequant.xprc.commands;

import java.util.Optional;

import de.energiequant.xprc.ChannelMessage;

public class SRIDMessage extends ChannelMessage {
    private final String key;
    private final String value;

    /**
     * Standard keys as defined by the protocol specification.
     */
    public enum StandardKey {
        /**
         * human-readable name of server implementation; for machine-readable identification use {@link #ID} instead
         */
        NAME(true),

        /**
         * machine-readable ID of server implementation
         */
        ID(true),

        /**
         * implementation-specific version string; only interpret according to server implementation
         */
        VERSION(true),

        /**
         * XPLM (X-Plane API) version as indicated by X-Plane ({@code XPLMGetVersions})
         */
        API_VERSION(),

        /**
         * server implementation website
         */
        WEBSITE(),

        /**
         * X-Plane version as indicated through X-Plane API ({@code XPLMGetVersions})
         */
        XP_VERSION();

        private final boolean mandatory;
        private final String encoding;

        StandardKey() {
            this(false);
        }

        StandardKey(boolean mandatory) {
            this.mandatory = mandatory;

            encoding = name().toLowerCase().replace("_", "");
        }

        public String getEncoding() {
            return encoding;
        }

        public boolean isMandatory() {
            return mandatory;
        }
    }

    SRIDMessage(ChannelMessage msg) {
        super(msg);

        String payload = msg.getRawPayload().orElse(null);
        if (!msg.containsData() || payload == null || payload.isEmpty()) {
            key = null;
            value = null;
            return;
        }

        int delimiterPos = payload.indexOf(':');
        if (delimiterPos < 0) {
            throw new IllegalArgumentException("payload is missing key delimiter");
        }

        key = payload.substring(0, delimiterPos);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("payload indicates empty key (invalid per spec)");
        }

        value = payload.substring(delimiterPos + 1);
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}

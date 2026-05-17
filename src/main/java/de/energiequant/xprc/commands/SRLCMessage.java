package de.energiequant.xprc.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import de.energiequant.xprc.ChannelMessage;
import de.energiequant.xprc.utils.ImmutablePair;

public class SRLCMessage extends ChannelMessage {
    private final SRLCDescriptor descriptor;

    private static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("^[A-Z0-9]{4}$");

    private static final int SUPPORTED_COMMAND_VERSION = 1;

    public static class SRLCDescriptor {
        private final String commandName;
        private final int activeVersion;
        private final Map<String, FeatureFlagStatus> featureFlags;

        private SRLCDescriptor(String commandName, int activeVersion, Map<String, FeatureFlagStatus> featureFlags) {
            this.commandName = commandName;
            this.activeVersion = activeVersion;
            this.featureFlags = featureFlags;
        }

        public String getCommandName() {
            return commandName;
        }

        public int getActiveVersion() {
            return activeVersion;
        }

        public Map<String, FeatureFlagStatus> getFeatureFlags() {
            return featureFlags;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("SRLCDescriptor(");
            sb.append(commandName);
            sb.append("/");
            sb.append(activeVersion);
            sb.append(", {");

            boolean first = true;
            for (Map.Entry<String, FeatureFlagStatus> entry : featureFlags.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(entry.getKey());
                sb.append(": ");
                sb.append(entry.getValue());
            }

            sb.append("})");

            return sb.toString();
        }
    }

    public enum FeatureFlagStatus {
        ENABLED_BY_DEFAULT('*', true, false, true),
        DISABLED_BY_DEFAULT('?', false, false, true),
        ENABLED_BY_REQUEST('+', true, true, true),
        DISABLED_BY_REQUEST('-', false, true, true),
        UNAVAILABLE('/', false, false, false),
        ALWAYS_ENABLED(null, true, false, false);

        private final Character encoding;
        private final boolean enabled;
        private final boolean requested;
        private final boolean modifiable;

        FeatureFlagStatus(Character encoding, boolean enabled, boolean requested, boolean modifiable) {
            this.encoding = encoding;
            this.enabled = enabled;
            this.requested = requested;
            this.modifiable = modifiable;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean hasBeenRequested() {
            return requested;
        }

        public boolean canBeModified() {
            return modifiable;
        }

        private static ImmutablePair<FeatureFlagStatus, String> parse(String s) {
            // identify flag by first character
            // "always enabled" flags has no prefix => default if unmatched
            FeatureFlagStatus status = ALWAYS_ENABLED;
            char ch = s.charAt(0);
            for (FeatureFlagStatus value : values()) {
                if (value.encoding != null && ch == value.encoding) {
                    status = value;
                    break;
                }
            }

            // remove status character from name
            String name = s;
            if (status.encoding != null) {
                name = name.substring(1);
            }

            if (name.isEmpty()) {
                throw new IllegalArgumentException("feature flag encoding must contain status and name but name was empty: \"" + s + "\"");
            }

            return ImmutablePair.of(status, name);
        }
    }

    SRLCMessage(ChannelMessage msg) {
        super(msg);

        String payload = msg.getRawPayload().orElse(null);
        if (!msg.containsData() || payload == null || payload.isEmpty()) {
            descriptor = null;
            return;
        }

        String[] parts = payload.split(":");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("unexpected number of parts (" + parts.length + ") in SRLC response: \"" + payload + "\"");
        }

        String commandName = parts[0];
        if (!COMMAND_NAME_PATTERN.matcher(commandName).matches()) {
            throw new IllegalArgumentException("invalid command name in SRLC response: \"" + payload + "\"");
        }

        int activeVersion;
        try {
            activeVersion = Integer.parseUnsignedInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("active command version could not be parsed from SRLC response: \"" + payload + "\"");
        }
        if (activeVersion < 0) {
            throw new IllegalArgumentException("illegal command version in SRLC response: \"" + payload + "\"");
        }

        Map<String, FeatureFlagStatus> featureFlags = new HashMap<>();
        if (parts.length > 2) {
            for (String encodedFeatureFlag : parts[2].split(",")) {
                ImmutablePair<FeatureFlagStatus, String> decoded = FeatureFlagStatus.parse(encodedFeatureFlag);
                FeatureFlagStatus previous = featureFlags.put(decoded.getRight(), decoded.getLeft());
                if (previous != null) {
                    throw new IllegalArgumentException("feature flag \"" + decoded.getRight() + "\" is specified multiple times in SRLC response: \"" + payload + "\"");
                }
            }
        }

        descriptor = new SRLCDescriptor(commandName, activeVersion, featureFlags);
    }

    public Optional<SRLCDescriptor> getDescriptor() {
        return Optional.ofNullable(descriptor);
    }

    public static boolean isExpectedCommandVersion(int commandVersion) {
        return commandVersion == SUPPORTED_COMMAND_VERSION;
    }

    public static SortedSet<Integer> getSupportedCommandVersions() {
        // FIXME: move to some factory as it is needed interface-wise and if we actually have more than one command version we will also need to instantiate specific handlers
        return new TreeSet<>(Collections.singletonList(SUPPORTED_COMMAND_VERSION));
    }
}

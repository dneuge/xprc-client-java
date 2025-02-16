package de.energiequant.xprc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Command<CFB extends ChannelFactoryBuilder<CFB, CH, C, M>, CH extends Channel<CH, C, M>, C extends Command<CFB, CH, C, M>, M extends ChannelMessage> {
    private final String name;
    private final Map<String, String> options;
    private final List<String> parameters;
    private final BiFunction<XPRCClient, Supplier<C>, CFB> channelFactoryBuilder;

    private static final char SECTION_DELIMITER = ' ';
    private static final char ESCAPE_CHARACTER = '\\';
    private static final char END_OF_OPTIONS = SECTION_DELIMITER;
    private static final char OPTION_KEY_VALUE_DELIMITER = '=';
    private static final char OPTION_DELIMITER = ';';
    private static final char PARAMETER_DELIMITER = ';';

    private Command(String name, Map<String, String> options, List<String> parameters, BiFunction<XPRCClient, Supplier<C>, CFB> channelFactoryBuilder) {
        // NOTE: This constructor has been intentionally hidden to ensure that all input has been checked by the builder
        //       to report issues where they are actually being entered (not just failing on construction) and to be
        //       able to trust that e.g. options do not need further sanitization when encoding.
        this.name = name;
        this.options = new LinkedHashMap<>(options);
        this.parameters = new ArrayList<>(parameters);
        this.channelFactoryBuilder = channelFactoryBuilder;
    }

    @SuppressWarnings("unchecked")
    public CFB createChannelFactoryBuilder(XPRCClient client) {
        return channelFactoryBuilder.apply(client, () -> (C) this);
    }

    public String encodeRequest(ChannelId channelId) {
        return encodeRequest(channelId.getAlphanumeric());
    }

    public String encodeRequest(String channelId) {
        StringBuilder sb = new StringBuilder(channelId);

        sb.append(SECTION_DELIMITER);
        sb.append(name);

        for (Map.Entry<String, String> option : options.entrySet()) {
            sb.append(OPTION_DELIMITER);
            sb.append(option.getKey());
            sb.append(OPTION_KEY_VALUE_DELIMITER);
            sb.append(option.getValue());
        }

        int numParams = parameters.size();
        if (numParams > 0) {
            boolean isFirst = true;
            for (String parameter : parameters) {
                if (isFirst) {
                    sb.append(SECTION_DELIMITER);
                    isFirst = false;
                } else {
                    sb.append(PARAMETER_DELIMITER);
                }

                sb.append(parameter);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Command(");
        sb.append(name);

        if (!options.isEmpty()) {
            boolean first = true;
            sb.append(", options={");
            for (Map.Entry<String, String> option : options.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(option.getKey());
                sb.append("=\"");
                sb.append(option.getValue());
                sb.append("\"");
            }
            sb.append("}");
        }

        if (!parameters.isEmpty()) {
            boolean first = true;
            sb.append(", params=[");
            for (String parameter : parameters) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append("\"");
                sb.append(parameter);
                sb.append("\"");
            }
            sb.append("]");
        }

        sb.append(")");

        return sb.toString();
    }

    public static class Builder<B extends Builder<B, C, CH, CFB, M>, C extends Command<CFB, CH, C, M>, CH extends Channel<CH, C, M>, CFB extends ChannelFactoryBuilder<CFB, CH, C, M>, M extends ChannelMessage> {
        private final String name;
        private final Map<String, String> options = new LinkedHashMap<>();
        private final List<String> parameters = new ArrayList<>();

        private final XPRCClient client;
        private final BiFunction<XPRCClient, Supplier<C>, CFB> channelFactoryBuilder;

        private static final Pattern PATTERN_COMMAND_NAME = Pattern.compile("^[A-Z]{4}$");

        // FIXME: document encoding restrictions in protocol spec

        private static final char[] PROHIBITED_CHARACTERS_OPTION_NAME = {
            OPTION_DELIMITER,
            OPTION_KEY_VALUE_DELIMITER,
            END_OF_OPTIONS,
            ESCAPE_CHARACTER // escapes are not supported in option names
        };

        private static final char[] PROHIBITED_CHARACTERS_OPTION_VALUE = {
            OPTION_DELIMITER,
            END_OF_OPTIONS,
            ESCAPE_CHARACTER // escapes are not supported in option values
        };

        private static final char[] PROHIBITED_CHARACTERS_PARAMETER = {
            PARAMETER_DELIMITER // FIXME: interim while escapes and server-side implementation are under review
        };

        /**
         * Use only if {@link #createChannelFactoryBuilder(XPRCClient, Supplier)} should be used to construct channel
         * factory builders, e.g. if channels need extra information that cannot be passed otherwise.
         */
        protected Builder(XPRCClient client, String name) {
            this(client, name, null);
        }

        public Builder(XPRCClient client, String name, BiFunction<XPRCClient, Supplier<C>, CFB> channelFactoryBuilder) {
            if (!PATTERN_COMMAND_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("Not a valid XPRC command name: \"" + name + "\"");
            }

            this.client = client;
            this.name = name;
            this.channelFactoryBuilder = channelFactoryBuilder;
        }

        /**
         * Implement only if no static reference is possible to be made during construction, e.g. if channels need extra
         * information that cannot be passed otherwise. Has no effect if a static reference was provided to the
         * {@link Builder} constructor.
         */
        protected CFB createChannelFactoryBuilder(XPRCClient client, Supplier<C> commandSupplier) {
            throw new IncompleteImplementation("Command.Builder implementation is incomplete: either a static ChannelFactoryBuilder Supplier must be provided at time of construction or createChannelFactoryBuilder must be overridden");
        }

        protected XPRCClient getClient() {
            // some commands may depend on implementation details of a server; providing access to the client allows
            // builders to query feature support etc. (assuming a connection has already been established)
            return client;
        }

        @SuppressWarnings("unchecked")
        public B setOption(String name, String value) {
            if (name.isEmpty() || !containsOnlySupportedCharacters(name, PROHIBITED_CHARACTERS_OPTION_NAME)) {
                throw new IllegalArgumentException("Not a valid XPRC option name: \"" + name + "\"");
            }

            if (!containsOnlySupportedCharacters(value, PROHIBITED_CHARACTERS_OPTION_VALUE)) {
                throw new IllegalArgumentException("Not a valid XPRC option value: \"" + value + "\"");
            }

            options.put(name, value);

            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addParameter(String parameter) {
            if (!containsOnlySupportedCharacters(parameter, PROHIBITED_CHARACTERS_PARAMETER)) {
                throw new IllegalArgumentException("Parameter cannot be encoded: \"" + parameter + "\"");
            }

            parameters.add(parameter);

            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setParameter(int index, String parameter) {
            if (parameters.size() == index) {
                return addParameter(parameter);
            }

            if (!containsOnlySupportedCharacters(parameter, PROHIBITED_CHARACTERS_PARAMETER)) {
                throw new IllegalArgumentException("Parameter cannot be encoded: \"" + parameter + "\"");
            }

            parameters.set(index, parameter);

            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B removeParameter(int index) {
            parameters.remove(index);

            return (B) this;
        }

        public int countParameters() {
            return parameters.size();
        }

        private boolean containsOnlySupportedCharacters(String s, char[] extraProhibitedChars) {
            for (char ch : s.toCharArray()) {
                // protocol is limited to US-ASCII, control characters are not allowed
                // https://en.wikipedia.org/wiki/ASCII
                if (ch < 0x20 || ch > 0x7E) {
                    return false;
                }

                for (char prohibitedChar : extraProhibitedChars) {
                    if (ch == prohibitedChar) {
                        return false;
                    }
                }
            }

            return true;
        }

        @SuppressWarnings("unchecked")
        public C build() {
            if (channelFactoryBuilder != null) {
                return (C) new Command<>(name, options, parameters, channelFactoryBuilder);
            } else {
                return (C) new Command<>(name, options, parameters, this::createChannelFactoryBuilder);
            }
        }

        @SuppressWarnings("unchecked")
        public CFB prepareChannel() {
            // cast is needed due to work around type confusion at compile time
            return client.prepareChannel(Builder.this);
        }

        public Optional<CH> submit() {
            return prepareChannel().submit();
        }

        public CH submit(ChannelId channelId) {
            return prepareChannel().submit(channelId);
        }
    }

    private static class IncompleteImplementation extends RuntimeException {
        IncompleteImplementation(String msg) {
            super(msg);
        }
    }
}

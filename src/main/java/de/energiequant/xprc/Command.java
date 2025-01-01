package de.energiequant.xprc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Command<M extends ChannelMessage> {
    private final String name;
    private final Map<String, String> options;
    private final List<String> parameters;
    private final Supplier<ChannelDecoder<M>> channelDecoderFactory;

    private static final char SECTION_DELIMITER = ' ';
    private static final char ESCAPE_CHARACTER = '\\';
    private static final char END_OF_OPTIONS = SECTION_DELIMITER;
    private static final char OPTION_KEY_VALUE_DELIMITER = '=';
    private static final char OPTION_DELIMITER = ';';
    private static final char PARAMETER_DELIMITER = ';';

    private Command(String name, Map<String, String> options, List<String> parameters, Supplier<ChannelDecoder<M>> channelDecoderFactory) {
        // NOTE: This constructor has been intentionally hidden to ensure that all input has been checked by the builder
        //       to report issues where they are actually being entered (not just failing on construction) and to be
        //       able to trust that e.g. options do not need further sanitization when encoding.
        this.name = name;
        this.options = new LinkedHashMap<>(options);
        this.parameters = new ArrayList<>(parameters);
        this.channelDecoderFactory = channelDecoderFactory;
    }

    public ChannelDecoder<M> createChannelDecoder() {
        return channelDecoderFactory.get();
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

    public static class Builder<B extends Builder<B, M, D>, M extends ChannelMessage, D extends ChannelDecoder<M>> {
        private final String name;
        private final Map<String, String> options = new LinkedHashMap<>();
        private final List<String> parameters = new ArrayList<>();
        private final Supplier<ChannelDecoder<M>> channelDecoderFactory;

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

        public Builder(String name, Supplier<ChannelDecoder<M>> channelDecoderFactory) {
            if (!PATTERN_COMMAND_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("Not a valid XPRC command name: \"" + name + "\"");
            }

            this.name = name;
            this.channelDecoderFactory = channelDecoderFactory;
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

        public Command<M> build() {
            return new Command<>(name, options, parameters, channelDecoderFactory);
        }
    }
}

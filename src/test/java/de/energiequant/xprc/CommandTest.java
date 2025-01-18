package de.energiequant.xprc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mockito;

import de.energiequant.xprc.utils.Maps;

class CommandTest {
    @ParameterizedTest
    @CsvSource({
        "AAAA, TEST, 'AAAA TEST'",
        "1Tst, TEST, '1Tst TEST'",
        "AAAA, DRLS, 'AAAA DRLS'",
        "1Tst, DRLS, '1Tst DRLS'",
    })
    void testEncodeRequest_minimal_returnsExpectedResult(String channelId, String commandName, String expectedResult) {
        // arrange
        Command<?, ?, ?, ?> command = createBuilder(commandName).build();

        // act
        String result = command.encodeRequest(channelId);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> provide_validOptions_expectedResult() {
        return Stream.of(
            Arguments.of(
                "AAAA",
                "NOPM",
                Maps.createUnmodifiableLinkedHashMap(
                    Maps.entry("plain", "text")
                ),
                "AAAA NOPM;plain=text"
            ),

            Arguments.of(
                "AAAA",
                "NOPM",
                Maps.createUnmodifiableLinkedHashMap(
                    Maps.entry("novalue", "")
                ),
                "AAAA NOPM;novalue="
            ),

            // maintain order of insertion
            // NOTE: While this is not required by protocol or implementation it helps readability for debugging and
            //       allows testing without having to define permutations while only needing marginal overhead.
            Arguments.of(
                "AAAA",
                "NOPM",
                Maps.createUnmodifiableLinkedHashMap(
                    Maps.entry("oooo", "0"),
                    Maps.entry("z", "1"),
                    Maps.entry("a", "2"),
                    Maps.entry("m", "3"),
                    Maps.entry("oo", "4")
                ),
                "AAAA NOPM;oooo=0;z=1;a=2;m=3;oo=4"
            ),

            // valid special characters
            Arguments.of(
                "AAAA",
                "NOPM",
                Maps.createUnmodifiableLinkedHashMap(
                    Maps.entry("someArray[1]", "isOkay"),
                    Maps.entry("dots.and_underscores", "mayBeUsed"),
                    Maps.entry("also", "-.,=/_#$%!*()\"[]")
                ),
                "AAAA NOPM;someArray[1]=isOkay;dots.and_underscores=mayBeUsed;also=-.,=/_#$%!*()\"[]"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provide_validOptions_expectedResult")
    void testEncodeRequest_validOptions_returnsExpectedResult(String channelId, String commandName, Map<String, String> options, String expectedResult) {
        // arrange
        Command.Builder<?, ?, ?, ?, ?, ?> builder = createBuilder(commandName);
        for (Map.Entry<String, String> option : options.entrySet()) {
            builder.setOption(option.getKey(), option.getValue());
        }
        Command<?, ?, ?, ?> command = builder.build();

        // act
        String result = command.encodeRequest(channelId);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> provide_validParameters_expectedResult() {
        return Stream.of(
            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "parameter"
                ),
                "AAAA PARM parameter"
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "first",
                    "second",
                    "third"
                ),
                "AAAA PARM first;second;third"
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "MixedCase",
                    "UPPERCASE",
                    "12345",
                    "123.4567890"
                ),
                "AAAA PARM MixedCase;UPPERCASE;12345;123.4567890"
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "._:+-(){}[]<>!\"$%&/="
                ),
                "AAAA PARM ._:+-(){}[]<>!\"$%&/="
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "already\\escaped"
                ),
                "AAAA PARM already\\escaped"
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    ""
                ),
                "AAAA PARM "
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "",
                    ""
                ),
                "AAAA PARM ;"
            ),

            Arguments.of(
                "AAAA",
                "PARM",
                Arrays.asList(
                    "a",
                    "",
                    "b"
                ),
                "AAAA PARM a;;b"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provide_validParameters_expectedResult")
    void testEncodeRequest_validParameters_returnsExpectedResult(String channelId, String commandName, Collection<String> parameters, String expectedResult) {
        // arrange
        Command.Builder<?, ?, ?, ?, ?, ?> builder = createBuilder(commandName);
        parameters.forEach(builder::addParameter);
        Command<?, ?, ?, ?> command = builder.build();

        // act
        String result = command.encodeRequest(channelId);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @Nested
    class BuilderTest {
        @SuppressWarnings({"rawtypes", "unchecked", "resource"})
        @ParameterizedTest
        @ValueSource(strings = {
            "",
            "A",
            "AA",
            "AAA",
            " AAAA",
            "AAAA ",
            " AAAA ",
            "....",
            "____",
            "ABC1",
        })
        void testConstructor_invalidCommandName_throwsIllegalArgumentException(String commandName) {
            // arrange
            XPRCClient client = Mockito.mock(XPRCClient.class, Answers.RETURNS_DEEP_STUBS);
            Supplier<ChannelDecoder<?>> channelDecoderFactory = Mockito.mock(Supplier.class, Answers.RETURNS_DEEP_STUBS);
            BiFunction<XPRCClient, Command, ChannelFactoryBuilder> channelFactoryBuilder = Mockito.mock(BiFunction.class, Answers.RETURNS_DEEP_STUBS);

            // act
            ThrowingCallable action = () -> new Command.Builder(client, commandName, channelDecoderFactory, channelFactoryBuilder);

            // assert
            assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class)
                                      .hasMessageContaining("Not a valid XPRC command name");
        }

        @SuppressWarnings("rawtypes")
        @ParameterizedTest
        @ValueSource(strings = {
            "",
            "option;delimiter",
            "startOf=value",
            "some\\escape",
            "options end",
            "string\0delimiter",
            "line\nend",
        })
        void testSetOption_invalidOptionName_throwsIllegalArgumentException(String optionName) {
            // arrange
            Command.Builder builder = createBuilder("TEST");

            // act
            ThrowingCallable action = () -> builder.setOption(optionName, "value");

            // assert
            assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class)
                                      .hasMessageContaining("Not a valid XPRC option name");
        }

        @SuppressWarnings("rawtypes")
        @ParameterizedTest
        @ValueSource(strings = {
            "semi;colon",
            "spaces terminate too",
            "string\0delimiter",
        })
        void testSetOption_invalidOptionValue_throwsIllegalArgumentException(String optionValue) {
            // arrange
            Command.Builder builder = createBuilder("TEST");

            // act
            ThrowingCallable action = () -> builder.setOption("key", optionValue);

            // assert
            assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class)
                                      .hasMessageContaining("Not a valid XPRC option value");
        }

        @SuppressWarnings("rawtypes")
        @ParameterizedTest
        @ValueSource(strings = {
            "semi;colon",
            "string\0delimiter",
        })
        void testSetOption_invalidParameter_throwsIllegalArgumentException(String parameter) {
            // arrange
            Command.Builder builder = createBuilder("TEST");

            // act
            ThrowingCallable action = () -> builder.addParameter(parameter);

            // assert
            assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class)
                                      .hasMessageContaining("Parameter cannot be encoded");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Command.Builder<?, ?, ?, ?, ?, ?> createBuilder(String commandName) {
        XPRCClient client = Mockito.mock(XPRCClient.class, Answers.RETURNS_DEEP_STUBS);
        Supplier<ChannelDecoder<?>> channelDecoderFactory = Mockito.mock(Supplier.class, Answers.RETURNS_DEEP_STUBS);
        BiFunction<XPRCClient, Command, ChannelFactoryBuilder> channelFactoryBuilder = Mockito.mock(BiFunction.class, Answers.RETURNS_DEEP_STUBS);
        return new Command.Builder(client, commandName, channelDecoderFactory, channelFactoryBuilder);
    }
}

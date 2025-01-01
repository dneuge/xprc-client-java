package de.energiequant.xprc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class ChannelMessageTest {
    static Stream<Arguments> provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload() {
        return Stream.of(
            Arguments.of(
                "+ACK ABCD 8721",
                ChannelMessage.Type.ACKNOWLEDGEMENT,
                ChannelId.fromAlphanumeric("ABCD"),
                8721,
                Optional.empty()
            ),

            Arguments.of(
                "+ACK a0E9 8721",
                ChannelMessage.Type.ACKNOWLEDGEMENT,
                ChannelId.fromAlphanumeric("a0E9"),
                8721,
                Optional.empty()
            ),

            Arguments.of(
                "-ACK ABCD 8871",
                ChannelMessage.Type.FINALIZATION,
                ChannelId.fromAlphanumeric("ABCD"),
                8871,
                Optional.empty()
            ),

            Arguments.of(
                "+ABCD 8723 1.0;5",
                ChannelMessage.Type.CONTINUATION,
                ChannelId.fromAlphanumeric("ABCD"),
                8723,
                Optional.of("1.0;5")
            ),

            Arguments.of(
                "-ABCD 9122 0.99;4",
                ChannelMessage.Type.FINALIZATION,
                ChannelId.fromAlphanumeric("ABCD"),
                9122,
                Optional.of("0.99;4")
            ),

            Arguments.of(
                "-ERR ABCD 4597 no such dataref: i/do/not/exist",
                ChannelMessage.Type.ERROR,
                ChannelId.fromAlphanumeric("ABCD"),
                4597,
                Optional.of("no such dataref: i/do/not/exist")
            ),

            Arguments.of(
                "-ACK ABCD 3487 int[],float[],blob",
                ChannelMessage.Type.FINALIZATION,
                ChannelId.fromAlphanumeric("ABCD"),
                3487,
                Optional.of("int[],float[],blob")
            ),

            Arguments.of(
                "+ACK ABCD 3487 int[],float[],blob",
                ChannelMessage.Type.ACKNOWLEDGEMENT,
                ChannelId.fromAlphanumeric("ABCD"),
                3487,
                Optional.of("int[],float[],blob")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload")
    void testGetRaw_validMessage_returnsExpectedResult(String expectedRaw, ChannelMessage.Type type, ChannelId channelId, int relativeTimestampMillis, Optional<String> rawPayload) {
        // arrange
        ChannelMessage msg = new ChannelMessage(Instant.now(), expectedRaw);

        // act
        String result = msg.getRaw();

        // assert
        assertThat(result).isEqualTo(expectedRaw);
    }

    @ParameterizedTest
    @MethodSource("provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload")
    void testGetType_validMessage_returnsExpectedResult(String raw, ChannelMessage.Type expectedType, ChannelId channelId, int relativeTimestampMillis, Optional<String> rawPayload) {
        // arrange
        ChannelMessage msg = new ChannelMessage(Instant.now(), raw);

        // act
        ChannelMessage.Type result = msg.getType();

        // assert
        assertThat(result).isEqualTo(expectedType);
    }

    @ParameterizedTest
    @MethodSource("provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload")
    void testGetChannelId_validMessage_returnsExpectedResult(String raw, ChannelMessage.Type type, ChannelId expectedChannelId, int relativeTimestampMillis, Optional<String> rawPayload) {
        // arrange
        ChannelMessage msg = new ChannelMessage(Instant.now(), raw);

        // act
        ChannelId result = msg.getChannelId();

        // assert
        assertThat(result).isEqualTo(expectedChannelId);
    }

    @ParameterizedTest
    @MethodSource("provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload")
    void testGetRelativeTimestampMillis_validMessage_returnsExpectedResult(String raw, ChannelMessage.Type type, ChannelId channelId, int expectedRelativeTimestampMillis, Optional<String> rawPayload) {
        // arrange
        ChannelMessage msg = new ChannelMessage(Instant.now(), raw);

        // act
        int result = msg.getRelativeTimestampMillis();

        // assert
        assertThat(result).isEqualTo(expectedRelativeTimestampMillis);
    }

    @ParameterizedTest
    @MethodSource("provide_validMessage_expectedTypeAndChannelIdAndRelativeTimestampAndPayload")
    void testGetRawPayload_validMessage_returnsExpectedResult(String raw, ChannelMessage.Type type, ChannelId channelId, int relativeTimestampMillis, Optional<String> expectedRawPayload) {
        // arrange
        ChannelMessage msg = new ChannelMessage(Instant.now(), raw);

        // act
        Optional<String> result = msg.getRawPayload();

        // assert
        assertThat(result).isEqualTo(expectedRawPayload);
    }

    // TODO: test format errors
    // TODO: test empty payload string (space after timestamp)
    // TODO: test timestamp calculation
}

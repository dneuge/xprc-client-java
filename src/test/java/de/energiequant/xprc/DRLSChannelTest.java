package de.energiequant.xprc;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.set;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;

import de.energiequant.xprc.types.ValueType;

class DRLSChannelTest {
    static Stream<Arguments> provide_payload_datarefTypesAndWritabilityAndName() {
        return Stream.of(
            Arguments.of(
                "int,float,double;rw;sim/some/ref",
                Arrays.asList(ValueType.INTEGER, ValueType.FLOAT, ValueType.DOUBLE),
                true,
                "sim/some/ref"
            ),

            Arguments.of(
                "blob;ro;something/else",
                Collections.singletonList(ValueType.BLOB),
                false,
                "something/else"
            ),

            Arguments.of(
                "int;rw;what/ever",
                Collections.singletonList(ValueType.INTEGER),
                true,
                "what/ever"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provide_payload_datarefTypesAndWritabilityAndName")
    void testDecode_validContinuationMessage_resultHoldsExpectedTypes(String payload, Collection<ValueType<?>> expectedTypes, boolean writability, String name) {
        // arrange
        Session session = Mockito.mock(Session.class, Answers.RETURNS_DEEP_STUBS);
        DRLSChannel<?, ?, ?> channel = new DRLSChannel<>(null, session, null);
        ChannelMessage channelMessage = new ChannelMessage(Instant.now(), "+AAAA 1234 " + payload);

        // act
        DRLSMessage result = channel.decode(channelMessage);

        // assert
        assertThat(result).extracting(DRLSMessage::getDataRefDescription)
                          .extracting(Optional::get)
                          .extracting(DRLSMessage.DataRefDescription::getTypes, as(set(ValueType.class)))
                          .containsExactlyInAnyOrderElementsOf(expectedTypes);
    }

    @ParameterizedTest
    @MethodSource("provide_payload_datarefTypesAndWritabilityAndName")
    void testDecode_validContinuationMessage_resultHoldsExpectedWritability(String payload, Collection<ValueType<?>> types, boolean expectedWritability, String name) {
        // arrange
        Session session = Mockito.mock(Session.class, Answers.RETURNS_DEEP_STUBS);
        DRLSChannel<?, ?, ?> channel = new DRLSChannel<>(null, session, null);
        ChannelMessage channelMessage = new ChannelMessage(Instant.now(), "+AAAA 1234 " + payload);

        // act
        DRLSMessage result = channel.decode(channelMessage);

        // assert
        assertThat(result).extracting(DRLSMessage::getDataRefDescription)
                          .extracting(Optional::get)
                          .extracting(DRLSMessage.DataRefDescription::isWritable)
                          .isEqualTo(expectedWritability);
    }

    @ParameterizedTest
    @MethodSource("provide_payload_datarefTypesAndWritabilityAndName")
    void testDecode_validContinuationMessage_resultHoldsExpectedName(String payload, Collection<ValueType<?>> types, boolean writability, String expectedName) {
        // arrange
        Session session = Mockito.mock(Session.class, Answers.RETURNS_DEEP_STUBS);
        DRLSChannel<?, ?, ?> channel = new DRLSChannel<>(null, session, null);
        ChannelMessage channelMessage = new ChannelMessage(Instant.now(), "+AAAA 1234 " + payload);

        // act
        DRLSMessage result = channel.decode(channelMessage);

        // assert
        assertThat(result).extracting(DRLSMessage::getDataRefDescription)
                          .extracting(Optional::get)
                          .extracting(DRLSMessage.DataRefDescription::getName)
                          .isEqualTo(expectedName);
    }

    // TODO: test other message containers
    // TODO: test parsing errors
}

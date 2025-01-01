package de.energiequant.xprc.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DoubleValueTypeTest {
    // FIXME: what about infinity and NaN? test with X-Plane and plugin and describe in protocol specification

    static Stream<Arguments> dataProviderSerialization() {
        // FIXME: Java serializes with uppercase E; confirm what formats the plugin is capable to parse on all platforms and adapt protocol specification accordingly
        return Stream.of(
            Arguments.of(0.0, "0.0"),
            Arguments.of(1.0, "1.0"),
            Arguments.of(1.5, "1.5"),
            Arguments.of(12.34, "12.34"),
            Arguments.of(-1.0, "-1.0"),
            Arguments.of(-1.5, "-1.5"),
            Arguments.of(-123.4, "-123.4"),
            Arguments.of(Double.MIN_VALUE, "4.9E-324"),
            Arguments.of(Double.MAX_VALUE, "1.7976931348623157E308")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testSerialize_serializable_returnsExpectedResult(double value, String expectedResult) {
        // arrange
        ValueType<Double> valueType = new DoubleValueType();

        // act
        String result = valueType.serialize(value);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testDeserialize_serializable_returnsExpectedResult(double expectedResult, String s) {
        // arrange
        ValueType<Double> valueType = new DoubleValueType();

        // act
        double result = valueType.deserialize(s);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }
}

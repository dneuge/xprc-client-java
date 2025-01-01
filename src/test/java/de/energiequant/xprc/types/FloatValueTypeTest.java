package de.energiequant.xprc.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FloatValueTypeTest {
    // FIXME: what about infinity and NaN? test with X-Plane and plugin and describe in protocol specification

    static Stream<Arguments> dataProviderSerialization() {
        // FIXME: Java serializes with uppercase E; confirm what formats the plugin is capable to parse on all platforms and adapt protocol specification accordingly
        return Stream.of(
            Arguments.of(0.0f, "0.0"),
            Arguments.of(1.0f, "1.0"),
            Arguments.of(1.5f, "1.5"),
            Arguments.of(12.34f, "12.34"),
            Arguments.of(-1.0f, "-1.0"),
            Arguments.of(-1.5f, "-1.5"),
            Arguments.of(-123.4f, "-123.4"),
            Arguments.of(Float.MIN_VALUE, "1.4E-45"),
            Arguments.of(Float.MAX_VALUE, "3.4028235E38")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testSerialize_serializable_returnsExpectedResult(float value, String expectedResult) {
        // arrange
        ValueType<Float> valueType = new FloatValueType();

        // act
        String result = valueType.serialize(value);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testDeserialize_serializable_returnsExpectedResult(float expectedResult, String s) {
        // arrange
        ValueType<Float> valueType = new FloatValueType();

        // act
        float result = valueType.deserialize(s);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }
}

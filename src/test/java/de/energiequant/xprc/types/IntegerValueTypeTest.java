package de.energiequant.xprc.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IntegerValueTypeTest {
    static Stream<Arguments> dataProviderSerialization() {
        return Stream.of(
            Arguments.of(0, "0"),
            Arguments.of(1, "1"),
            Arguments.of(5, "5"),
            Arguments.of(10, "10"),
            Arguments.of(-1, "-1"),
            Arguments.of(-42, "-42"),
            Arguments.of(Integer.MIN_VALUE, "-2147483648"),
            Arguments.of(Integer.MAX_VALUE, "2147483647")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testSerialize_always_returnsExpectedResult(Integer value, String expectedResult) {
        // arrange
        ValueType<Integer> valueType = new IntegerValueType();

        // act
        String result = valueType.serialize(value);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testDeserialize_always_returnsExpectedResult(Integer expectedResult, String s) {
        // arrange
        ValueType<Integer> valueType = new IntegerValueType();

        // act
        Integer result = valueType.deserialize(s);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }
}

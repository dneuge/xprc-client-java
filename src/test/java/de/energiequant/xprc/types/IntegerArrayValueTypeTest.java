package de.energiequant.xprc.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class IntegerArrayValueTypeTest {
    // FIXME: Are empty arrays valid? (in X-Plane, mainly)
    // FIXME: What is the maximum array length we can transmit? Test and specify in protocol.

    static Stream<Arguments> dataProviderSerialization() {
        return Stream.of(
            Arguments.of(new int[]{0}, "1,0"),
            Arguments.of(new int[]{5, 23, 42}, "3,5,23,42"),
            Arguments.of(new int[]{-100, 98, 25, -5, -7, 9, 10}, "7,-100,98,25,-5,-7,9,10")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testSerialize_always_returnsExpectedResult(int[] values, String expectedResult) {
        // arrange
        ValueType<int[]> valueType = new IntegerArrayValueType();

        // act
        String result = valueType.serialize(values);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("dataProviderSerialization")
    void testDeserialize_valid_returnsExpectedResult(int[] expectedResult, String s) {
        // arrange
        ValueType<int[]> valueType = new IntegerArrayValueType();

        // act
        int[] result = valueType.deserialize(s);

        // assert
        assertThat(result).containsExactly(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "0",
        "1",
        "1,",
        ",1",
        "0,1",
        "2,1",
        "2,1,",
        "2,,1",
        "2,,1,",
        "2,1,2,3",
        "-2,1,2",
        "-2,",
        "-2",
        "1.0,1",
        "1,1.0",
        "1,1e0",
        "1,1e1",
        "1,1e-1",
        "1,A0",
    })
    void testDeserialize_invalid_throwsIllegalArgumentException(String s) {
        // arrange
        ValueType<int[]> valueType = new IntegerArrayValueType();

        // act
        ThrowingCallable action = () -> valueType.deserialize(s);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
}

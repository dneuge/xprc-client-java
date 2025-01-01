package de.energiequant.xprc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.energiequant.xprc.utils.MathUtils;

class ChannelIdTest {
    private static final char[] CHARACTERS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
        'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    private static final int CHANNEL_ID_LENGTH = 4;
    private static final int MAX_CHANNELS = MathUtils.intPow(CHARACTERS.length, CHANNEL_ID_LENGTH);

    @Test
    void testToAlphanumeric_fullRange_resultConvertsBackToOriginalNumericValue() {
        // this test deviates from standard arrange-act-assert and generator patterns to make it more
        // manageable to execute; we still only test a single aspect per test case

        for (int original = 0; original < MAX_CHANNELS; original++) {
            String alphanumeric = ChannelId.toAlphanumeric(original);
            int result = ChannelId.toNumeric(alphanumeric);

            assertThat(result).isEqualTo(original);
        }
    }

    @Test
    void testToNumeric_fullRange_resultConvertsBackToOriginalAlphanumericValue() {
        // this test deviates from standard arrange-act-assert and generator patterns to make it more
        // manageable to execute; we still only test a single aspect per test case

        int[] address = new int[CHANNEL_ID_LENGTH];
        for (int i = 0; i < MAX_CHANNELS; i++) {
            // address to alphanumeric string
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < address.length; j++) {
                sb.append(CHARACTERS[address[j]]);
            }
            String original = sb.toString();

            // test conversion
            int numeric = ChannelId.toNumeric(original);
            String result = ChannelId.toAlphanumeric(numeric);
            assertThat(result).isEqualTo(original);

            // increment address by one
            int carry = 0;
            address[address.length - 1]++;
            for (int j = address.length - 1; j >= 0; j--) {
                address[j] += carry;

                if (address[j] < CHARACTERS.length) {
                    carry = 0;
                } else {
                    carry = 1;
                    address[j] -= CHARACTERS.length;
                }
            }
        }
    }

    private static Stream<Arguments> provide_invalidAlphanumericChannelIds() {
        return Stream.of(
            null,
            "",

            " AAAA",
            "AAAA ",
            "AA AA",
            " AAAA ",

            "    ",
            "----",
            "AA-A",

            "00000",
            "AAAAA",
            "aaaaa",
            "zzzz0",
            "0zzzz"
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provide_invalidAlphanumericChannelIds")
    void testToNumeric_invalidAlphanumericChannelId_throwsIllegalArgumentException(String invalidId) {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> ChannelId.toNumeric(invalidId);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("provide_invalidAlphanumericChannelIds")
    void testFromAlphanumeric_invalidAlphanumericChannelId_throwsIllegalArgumentException(String invalidId) {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> ChannelId.fromAlphanumeric(invalidId);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
}

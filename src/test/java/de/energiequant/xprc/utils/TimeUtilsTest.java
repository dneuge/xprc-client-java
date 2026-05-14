package de.energiequant.xprc.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TimeUtilsTest {
    @ParameterizedTest
    @CsvSource({
        // timestamps converted to epoch seconds on Linux using: date -d'2026-06-01T12:00:00.000+00:00' +%s

        // real-world offsets
        "'2026-06-01T12:00:00.000Z',      1780315200000",
        "'2026-06-01T12:00:00.000+00:00', 1780315200000",
        "'2026-06-01T12:00:00.000-12:00', 1780358400000", // western-most offset in use
        "'2026-06-01T12:00:00.000-01:00', 1780318800000",
        "'2026-06-01T12:00:00.000+02:00', 1780308000000",
        "'2026-06-01T12:00:00.000+05:30', 1780295400000",
        "'2026-06-01T12:00:00.000+05:45', 1780294500000",
        "'2026-06-01T12:00:00.000+14:00', 1780264800000", // eastern-most offset in use

        // only seconds
        "'2026-06-01T12:00:00Z',          1780315200000",

        // milliseconds
        "'2026-06-01T12:00:00.123Z',      1780315200123",
        "'2026-06-01T12:00:00.999Z',      1780315200999",

        // deciseconds, centiseconds (unusual but possible)
        "'2026-06-01T12:00:00.1Z',        1780315200100",
        "'2026-06-01T12:00:00.12Z',       1780315200120",

        // year change
        "'2026-01-01T00:00:00.000Z',      1767225600000",
        "'2025-12-31T23:00:00.000-01:00', 1767225600000",
        "'2026-01-01T01:00:00.000+01:00', 1767225600000",

        // check that actual offsets are parsed without any translation to timezones
        // (ensures that unknown offsets/timezones can also be handled by the client)
        "'2026-06-01T12:00:00.000-13:12', 1780362720000", // fictional arbitrary offset (no matching timezone)
        "'2026-03-29T02:10:00.000+01:00', 1774746600000", // DST change Europe/Berlin
        "'2026-03-08T02:10:00.000-06:00', 1772957400000", // DST change America/Denver

        // Y2K38
        "'2038-01-19T03:14:07.000Z',      2147483647000", // epoch seconds still compatible with signed 32 bit int
        "'2038-01-19T03:14:08.000Z',      2147483648000", // epoch seconds need 64 bits (or unsigned int)
    })
    void testParseOffsetTimestamp_validStandardPrecision_returnsExpectedInstant(String s, long expectedEpochMilli) {
        // arrange
        Instant expectedResult = Instant.ofEpochMilli(expectedEpochMilli);

        // act
        Instant result = TimeUtils.parseOffsetTimestamp(s);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
        // microseconds
        "'2026-06-01T12:00:00.123456Z',    1780315200123, 456000",
        "'2026-06-01T12:00:00.999999Z',    1780315200999, 999000",

        // nanoseconds
        "'2026-06-01T12:00:00.123456789Z', 1780315200123, 456789",
        "'2026-06-01T12:00:00.999999999Z', 1780315200999, 999999",
    })
    void testParseOffsetTimestamp_validHighPrecision_returnsExpectedInstant(String s, long expectedEpochMilli, long expectedExtraNanos) {
        // arrange
        Instant expectedResult = Instant.ofEpochMilli(expectedEpochMilli)
                                        .plusNanos(expectedExtraNanos);

        // act
        Instant result = TimeUtils.parseOffsetTimestamp(s);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }
}

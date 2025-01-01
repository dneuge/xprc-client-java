package de.energiequant.xprc.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringsTest {
    private static final Map<String, String> VARIABLES = Maps.createUnmodifiableHashMap(
        Maps.entry("a", "substituted a"),
        Maps.entry("B", "substituted B"),
        Maps.entry("something", "something got substituted"),
        Maps.entry("somethingElse", "substituted something else"),
        Maps.entry("recursive", "recursions like ${something} should not be substituted"),
        Maps.entry("UPPERCASE", "upper case"),
        Maps.entry("lowercase", "lower case"),
        Maps.entry("collision", "lower case"),
        Maps.entry("CoLLiSION", "mixed case"),
        Maps.entry("COLLISION", "upper case")
    );

    @ParameterizedTest
    @CsvSource({
        // no substitution at all
        "'', ''",
        "' ', ' '",
        "no substitution, no substitution",
        "{a}, {a}",

        // full substitution
        "${a}, substituted a",
        "${B}, substituted B",
        "${something}, something got substituted",
        "${collision}, lower case",
        "${CoLLiSION}, mixed case",
        "${COLLISION}, upper case",
        "${recursive}, recursions like ${something} should not be substituted",

        // surrounding characters
        "' ${a}', ' substituted a'",
        "'${a} ', 'substituted a '",
        "'  ${a}  ', '  substituted a  '",
        "'Hey, ${something} - cool!', 'Hey, something got substituted - cool!'",
        "${a}}, substituted a}",

        // multiple substitutions
        "${a}${B}, substituted asubstituted B",
        "${a} ${B}, substituted a substituted B",
        "' ${a}${B}', ' substituted asubstituted B'",
        "'${a}${B} ', 'substituted asubstituted B '",
        "${a}${a}, substituted asubstituted a",
    })
    void testSubstituteVariables_caseSensitive_returnsExpectedSubstitution(String original, String expectedResult) {
        // arrange (nothing to do)

        // act
        String result = Strings.substituteVariables(original, true, VARIABLES);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
        // no substitution at all
        "'', ''",
        "' ', ' '",
        "no substitution, no substitution",
        "{a}, {a}",

        // full substitution
        "${a}, substituted a",
        "${A}, substituted a",
        "${b}, substituted B",
        "${B}, substituted B",
        "${something}, something got substituted",
        "${someThing}, something got substituted",
        "${recursive}, recursions like ${something} should not be substituted",

        // surrounding characters
        "' ${a}', ' substituted a'",
        "' ${A}', ' substituted a'",
        "'${a} ', 'substituted a '",
        "'${A} ', 'substituted a '",
        "'  ${a}  ', '  substituted a  '",
        "'  ${A}  ', '  substituted a  '",
        "'Hey, ${something} - cool!', 'Hey, something got substituted - cool!'",
        "'Hey, ${SomeThing} - cool!', 'Hey, something got substituted - cool!'",
        "${a}}, substituted a}",
        "${A}}, substituted a}",

        // multiple substitutions
        "${a}${B}, substituted asubstituted B",
        "${A}${b}, substituted asubstituted B",
        "${a} ${B}, substituted a substituted B",
        "${A} ${b}, substituted a substituted B",
        "' ${a}${B}', ' substituted asubstituted B'",
        "' ${A}${b}', ' substituted asubstituted B'",
        "'${a}${B} ', 'substituted asubstituted B '",
        "'${A}${b} ', 'substituted asubstituted B '",
        "${a}${a}, substituted asubstituted a",
        "${a}${A}, substituted asubstituted a",
        "${A}${a}, substituted asubstituted a",
        "${A}${A}, substituted asubstituted a",
    })
    void testSubstituteVariables_caseInsensitive_returnsExpectedSubstitution(String original, String expectedResult) {
        // arrange (nothing to do)

        // act
        String result = Strings.substituteVariables(original, false, VARIABLES);

        // assert
        assertThat(result).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "$",
        "${",
        "${}",
        "$a",
        "$a{a}",
    })
    void testSubstituteVariables_syntaxError_throwsIllegalArgumentException(String s) {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> Strings.substituteVariables(s, true, VARIABLES);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSubstituteVariables_wrongCase_throwsIllegalArgumentException() {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> Strings.substituteVariables("${b}", true, VARIABLES);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSubstituteVariables_undefinedVariable_throwsIllegalArgumentException() {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> Strings.substituteVariables("${unknown}", true, VARIABLES);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSubstituteVariables_collidingVariableUsed_throwsIllegalArgumentException() {
        // arrange (nothing to do)

        // act
        ThrowingCallable action = () -> Strings.substituteVariables("${collision}", false, VARIABLES);

        // assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
}

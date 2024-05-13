package dev.slav.argb.model.test

import dev.slav.argb.model.colorFrom565RGB
import dev.slav.argb.model.to565RGB
import java.awt.Color
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("Unit tests for Color 565 RGB utilities")
class Color565EGBTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName(
        value = "GIVEN a color, " +
                "WHEN to565RGB, " +
                "THEN return 565 RGB value for this color"
    )
    @MethodSource("to565RGBArguments")
    fun to565RGB(color: Color, expected: Int) {
        val actual = color.to565RGB()

        assertThat(actual).isEqualTo(expected.toUShort())
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName(
        value = "GIVEN a color, " +
                "WHEN to565RGB, " +
                "THEN return 565 RGB value for this color"
    )
    @MethodSource("colorFrom565RGBArguments")
    fun colorFrom565RGB(value: Int, expected: Color) {
        val actual = colorFrom565RGB(value.toUShort())

        assertThat(actual).isEqualTo(expected)
    }

    companion object {

        @JvmStatic
        fun to565RGBArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Color.red, 0xf800),
                Arguments.of(Color.green, 0x07e0),
                Arguments.of(Color.blue, 0x001f),
                Arguments.of(Color.cyan, 0x07ff),
                Arguments.of(Color.magenta, 0xf81f),
                Arguments.of(Color.yellow, 0xffe0),
                Arguments.of(Color.white, 0xffff),
                Arguments.of(Color.black, 0x0000)
            )

        @JvmStatic
        fun colorFrom565RGBArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of(0xf800, Color(0xf8, 0x00, 0x00)),
                Arguments.of(0x07e0, Color(0x00, 0xfc, 0x00)),
                Arguments.of(0x001f, Color(0x00, 0x00, 0xf8)),
                Arguments.of(0x07ff, Color(0x00, 0xfc, 0xf8)),
                Arguments.of(0xf81f, Color(0xf8, 0x00, 0xf8)),
                Arguments.of(0xffe0, Color(0xf8, 0xfc, 0x00)),
                Arguments.of(0xffff, Color(0xf8, 0xfc, 0xf8)),
                Arguments.of(0x0000, Color.black)
            )
    }
}

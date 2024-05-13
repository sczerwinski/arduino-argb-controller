package dev.slav.argb.model.commands.test

import dev.slav.argb.model.LEDFrame
import dev.slav.argb.model.commands.WriteCommand
import java.awt.Color
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Unit tests for WriteCommand")
class WriteCommandTest {

    @Test
    @DisplayName(
        value = "GIVEN WriteCommand, " +
                "WHEN toString(), " +
                "THEN return 'BEGIN 1 <data> END'"
    )
    fun string() {
        val white = Color.white
        val classUnderTest = WriteCommand(
            frameDelay = 123.toUShort(),
            frames = listOf(
                LEDFrame(colors = listOf(white, white, white, white, white, white, white, white)),
                LEDFrame(colors = listOf(white, white, white, white, white, white, white, white))
            )
        )

        val actual = classUnderTest.toString()

        assertThat(actual)
            .isEqualTo(
                "BEGIN 1 " +
                        "2 123 " +
                        "65535 65535 65535 65535 65535 65535 65535 65535 " +
                        "65535 65535 65535 65535 65535 65535 65535 65535 " +
                        "END"
            )
    }

    @Test
    @DisplayName(
        value = "GIVEN empty list of frames, " +
                "WHEN create new WriteCommand, " +
                "THEN throw IllegalArgumentException"
    )
    fun createEmpty() {
        assertThatThrownBy {
            WriteCommand(frameDelay = 123.toUShort(), frames = emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}

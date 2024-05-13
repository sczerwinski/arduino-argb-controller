package dev.slav.argb.model.commands.test

import dev.slav.argb.model.commands.ReadCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Unit tests for ReadCommand")
class ReadCommandTest {

    @Test
    @DisplayName(
        value = "GIVEN ReadCommand, " +
                "WHEN toString(), " +
                "THEN return 'BEGIN 0 END'"
    )
    fun string() {
        val actual = ReadCommand.toString()

        assertThat(actual).isEqualTo("BEGIN 0 END")
    }
}

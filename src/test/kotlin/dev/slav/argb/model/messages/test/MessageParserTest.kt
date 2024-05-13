package dev.slav.argb.model.messages.test

import dev.slav.argb.model.messages.DataMessage
import dev.slav.argb.model.messages.DoneMessage
import dev.slav.argb.model.messages.ErrorMessage
import dev.slav.argb.model.messages.InitMessage
import dev.slav.argb.model.messages.MessageParser
import dev.slav.argb.model.messages.NullMessage
import dev.slav.argb.model.messages.SetDataMessage
import dev.slav.argb.model.messages.UnsupportedMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Unit tests for MessageParser")
class MessageParserTest {

    private val classUnderTest = MessageParser()

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and null message, " +
                "WHEN parse(), " +
                "THEN return NullMessage"
    )
    fun parseNull() {
        val messageString: String? = null

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isSameAs(NullMessage)
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'INIT', " +
                "WHEN parse(), " +
                "THEN return InitMessage"
    )
    fun parseInit() {
        val messageString = "INIT"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isSameAs(InitMessage)
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DONE', " +
                "WHEN parse(), " +
                "THEN return DoneMessage"
    )
    fun parseDone() {
        val messageString = "DONE"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isSameAs(DoneMessage)
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DATA name 1 2 3', " +
                "WHEN parse(), " +
                "THEN return DataMessage('name', [1, 2, 3])"
    )
    fun parseData() {
        val messageString = "DATA name 1 2 3"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(DataMessage(name = "name", values = listOf(1u, 2u, 3u)))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DATA name', " +
                "WHEN parse(), " +
                "THEN return DataMessage('name', [])"
    )
    fun parseEmptyData() {
        val messageString = "DATA name"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(DataMessage(name = "name", values = emptyList()))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DATA', " +
                "WHEN parse(), " +
                "THEN return UnsupportedMessage('DATA', [])"
    )
    fun parseNoNameData() {
        val messageString = "DATA"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(UnsupportedMessage(type = "DATA"))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DATA name 1 a 3', " +
                "WHEN parse(), " +
                "THEN return DataMessage('name', [1, 3])"
    )
    fun parseNonNumberData() {
        val messageString = "DATA name 1 a 3"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(DataMessage(name = "name", values = listOf(1u, 3u)))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'SET name 1 2 3', " +
                "WHEN parse(), " +
                "THEN return SetDataMessage('name', [1, 2, 3])"
    )
    fun parseSet() {
        val messageString = "SET name 1 2 3"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(SetDataMessage(name = "name", values = listOf(1u, 2u, 3u)))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'DATA name', " +
                "WHEN parse(), " +
                "THEN return SetDataMessage('name', [])"
    )
    fun parseEmptySet() {
        val messageString = "SET name"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(SetDataMessage(name = "name", values = emptyList()))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'SET', " +
                "WHEN parse(), " +
                "THEN return UnsupportedMessage('SET', [])"
    )
    fun parseNoNameSet() {
        val messageString = "SET"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(UnsupportedMessage(type = "SET"))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'SET name 123 abc 456', " +
                "WHEN parse(), " +
                "THEN return SetDataMessage('name', [123, 456])"
    )
    fun parseNonNumberSet() {
        val messageString = "SET name 123 abc 456"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(SetDataMessage(name = "name", values = listOf(123u, 456u)))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'ERR Test error occurred', " +
                "WHEN parse(), " +
                "THEN return ErrorMessage('Test error occurred')"
    )
    fun parseError() {
        val messageString = "ERR Test error occurred"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(ErrorMessage(errorMessage = "Test error occurred"))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and message 'FOO abc 123 !@#', " +
                "WHEN parse(), " +
                "THEN return UnsupportedMessage('FOO', ['abc', '123', '!@#'])"
    )
    fun parseUnsupportedMessageType() {
        val messageString = "FOO abc 123 !@#"

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(UnsupportedMessage(type = "FOO", args = listOf("abc", "123", "!@#")))
    }

    @Test
    @DisplayName(
        value = "GIVEN MessageParser and empty message, " +
                "WHEN parse(), " +
                "THEN return UnsupportedMessage('', [])"
    )
    fun parseEmptyString() {
        val messageString = ""

        val actual = classUnderTest.parseMessage(messageString)

        assertThat(actual).isEqualTo(UnsupportedMessage(type = ""))
    }
}

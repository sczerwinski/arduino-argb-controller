package dev.slav.argb.model.microcontrollers.test

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortIOException
import dev.slav.argb.model.commands.ReadCommand
import dev.slav.argb.model.messages.InitMessage
import dev.slav.argb.model.messages.Message
import dev.slav.argb.model.microcontrollers.SerialMicrocontroller
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.capture
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@DisplayName("Unit tests for SerialMicrocontroller")
@ExtendWith(MockitoExtension::class)
class SerialMicrocontrollerTest {

    private val testScheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    private val testCoroutineScope = CoroutineScope(testDispatcher)

    @Mock
    lateinit var serialPort: SerialPort

    @Mock
    lateinit var inputStream: InputStream

    @Mock
    lateinit var messagesCollector: FlowCollector<Message>

    @Captor
    lateinit var bytesCaptor: ArgumentCaptor<ByteArray>

    @Captor
    lateinit var intCaptor: ArgumentCaptor<Int>

    @BeforeEach
    fun initSerialPortDetails() {
        whenever(serialPort.systemPortName) doReturn "COM2"
        whenever(serialPort.descriptivePortName) doReturn "USB-SERIAL (COM2)"
        whenever(serialPort.baudRate) doReturn 115200
    }

    @Test
    @DisplayName(
        value = "GIVEN serial port, " +
                "WHEN create new SerialMicrocontroller, " +
                "THEN initialize and open serial port"
    )
    fun create() {
        whenever(serialPort.inputStream) doReturn InputStream.nullInputStream()
        SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        verify(serialPort).baudRate = 115200
        verify(serialPort).openPort()
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller on COM2, " +
                "WHEN name, " +
                "THEN return 'COM2'"
    )
    fun name() {
        whenever(serialPort.inputStream) doReturn InputStream.nullInputStream()
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        val actual = classUnderTest.name

        assertThat(actual).isEqualTo("COM2")
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller, " +
                "AND 'INIT' message is read from input stream, " +
                "WHEN pullMessages, " +
                "THEN messageFlow should emit InitMessage"
    )
    fun pullMessages() {
        whenever(serialPort.inputStream) doReturn "INIT\r\n".encodeToByteArray().inputStream()
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        testCoroutineScope.launch {
            classUnderTest.messageFlow.collect(messagesCollector)
        }

        testCoroutineScope.launch {
            classUnderTest.pullMessages()
        }

        messagesCollector.stub { mockedCollector ->
            verifyBlocking(mockedCollector, times(numInvocations = 1)) { emit(InitMessage) }
        }
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller, " +
                "WHEN sendCommand, " +
                "THEN the command should be written to the serial port"
    )
    fun sendCommand() {
        whenever(serialPort.inputStream) doReturn InputStream.nullInputStream()
        whenever(serialPort.writeBytes(capture(bytesCaptor), capture(intCaptor))) doAnswer invocation@{ invocation ->
            return@invocation invocation.arguments.last() as Int
        }
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        testCoroutineScope.launch {
            classUnderTest.sendCommand(ReadCommand)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        testScheduler.advanceTimeBy(delayTimeMillis = 1000L)

        verify(serialPort, atLeastOnce()).writeBytes(any(), any())
        assertThat(bytesCaptor.allValues.joinToString(separator = "") { it.decodeToString() })
            .containsPattern("BEGIN\\s+0\\s+END")
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller, " +
                "AND serial port writes fewer bytes than expected, " +
                "WHEN sendCommand, " +
                "THEN throw SerialPortIOException"
    )
    fun sendCommandError() {
        whenever(serialPort.inputStream) doReturn InputStream.nullInputStream()
        whenever(serialPort.writeBytes(capture(bytesCaptor), capture(intCaptor))) doAnswer invocation@{ invocation ->
            return@invocation (invocation.arguments.last() as Int) - 1
        }
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        val job = testCoroutineScope.async {
            classUnderTest.sendCommand(ReadCommand)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        testScheduler.advanceTimeBy(delayTimeMillis = 1000L)

        assertThatThrownBy {
            runBlocking { job.await() }
        }.isInstanceOf(SerialPortIOException::class.java)
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller, " +
                "WHEN dispose, " +
                "THEN close input stream and serial port"
    )
    fun dispose() {
        whenever(serialPort.inputStream) doReturn inputStream
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        classUnderTest.dispose()

        verify(inputStream).close()
        verify(serialPort).closePort()
    }

    @Test
    @DisplayName(
        value = "GIVEN SerialMicrocontroller, " +
                "AND serial port input stream throws exception on close, " +
                "WHEN dispose, " +
                "THEN close input stream and serial port"
    )
    fun disposeWithError() {
        whenever(inputStream.close()) doThrow IOException("Test exception")
        whenever(serialPort.inputStream) doReturn inputStream
        val classUnderTest = SerialMicrocontroller(serialPort, dispatcher = testDispatcher)

        classUnderTest.dispose()

        verify(inputStream).close()
        verify(serialPort).closePort()
    }
}

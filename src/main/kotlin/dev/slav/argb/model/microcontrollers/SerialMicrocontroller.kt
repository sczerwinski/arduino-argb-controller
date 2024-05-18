package dev.slav.argb.model.microcontrollers

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortIOException
import dev.slav.argb.model.commands.Command
import dev.slav.argb.model.messages.Message
import dev.slav.argb.model.messages.MessageParser
import dev.slav.argb.model.messages.NullMessage
import java.io.IOException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * Microcontroller connected via serial port.
 */
class SerialMicrocontroller(
    /**
     * The serial port.
     */
    private val serialPort: SerialPort,

    /**
     * Coroutine dispatcher for the operations on the serial port.
     */
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Microcontroller {

    private val logger = LoggerFactory.getLogger("${javaClass.name}[$name]")

    /**
     * Name of this microcontroller.
     */
    override val name: String get() = serialPort.systemPortName

    private val _messageFlow = MutableStateFlow<Message>(value = NullMessage)

    /**
     * Flow of messages from the microcontroller.
     */
    override val messageFlow: Flow<Message> get() = _messageFlow

    private val serialPortReader = serialPort.inputStream.bufferedReader()

    private val messageParser = MessageParser()

    init {
        serialPort.baudRate = BAUD_RATE
        serialPort.setComPortTimeouts(TIMEOUT_MODE, READ_TIMEOUT, WRITE_TIMEOUT)
        serialPort.openPort()
        logger.info("Connection open: {} @ {} baud", this, serialPort.baudRate)
    }

    /**
     * Pulls [messages][Message] from the microcontroller.
     *
     * The pulled messages will be available in the [messageFlow].
     *
     * This method should be called in an infinite loop, in an interval of
     * less than one second.
     *
     * @throws IOException if pulling the messages fails.
     */
    @kotlin.jvm.Throws(IOException::class)
    override suspend fun pullMessages() {
        linesFlow()
            .takeWhile { line -> line != null }
            .collect { line ->
                logger.debug("--> {}", line)
                val message = messageParser.parseMessage(line)
                _messageFlow.emit(message)
            }
    }

    private suspend fun linesFlow(): Flow<String?> = flow {
        while (true) {
            val line = readLine()
            emit(line)
        }
    }

    private suspend fun readLine(): String? =
        withContext(context = coroutineContext + dispatcher) {
            serialPortReader.readLine()
        }

    /**
     * Sends a [command] to the microcontroller via serial port.
     *
     * @param command The command being sent to the microcontroller.
     *
     * @throws IOException if sending the command fails.
     */
    @Throws(IOException::class)
    override suspend fun sendCommand(command: Command) {
        logger.debug("<-- {}", command)
        val words = command.toString().split("\\s".toRegex())
        withContext(context = coroutineContext + dispatcher) {
            for (word in words) {
                logger.trace("<-- {}", word)
                val bytes = (word + "\n").encodeToByteArray()
                val bytesWritten = serialPort.writeBytes(bytes, bytes.size)
                if (bytesWritten < bytes.size) {
                    logger.error("<!-- Error: {}/{} bytes sent", bytesWritten, bytes.size)
                    throw SerialPortIOException("Error sending command: $bytesWritten/${bytes.size} bytes sent")
                }
                delay(timeMillis = SEND_WORD_DELAY)
            }
        }
    }

    /**
     * Cancels all jobs, closes the emitting dispatcher, and closes the serial port.
     */
    override fun dispose() {
        try {
            serialPortReader.close()
        } catch (e: IOException) {
            logger.error("Error closing serial port reader", e)
        }
        serialPort.closePort()
        logger.info("Connection closed: {}", this)
    }

    /**
     * Returns a descriptive name of the [serialPort].
     *
     * @return Descriptive name of the [serialPort].
     */
    override fun toString(): String =
        serialPort.descriptivePortName

    companion object {
        private const val BAUD_RATE = 115200

        private const val TIMEOUT_MODE = SerialPort.TIMEOUT_READ_BLOCKING or SerialPort.TIMEOUT_WRITE_BLOCKING
        private const val READ_TIMEOUT = 100
        private const val WRITE_TIMEOUT = 5000

        private const val SEND_WORD_DELAY = 20L
    }
}

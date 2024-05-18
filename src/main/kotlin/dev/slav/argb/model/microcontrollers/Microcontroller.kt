package dev.slav.argb.model.microcontrollers

import dev.slav.argb.model.Disposable
import dev.slav.argb.model.commands.Command
import dev.slav.argb.model.messages.Message
import java.io.IOException
import kotlin.jvm.Throws
import kotlinx.coroutines.flow.Flow

/**
 * Microcontroller communication interface.
 */
interface Microcontroller : Disposable {

    /**
     * Name of this microcontroller.
     */
    val name: String

    /**
     * Flow of messages from the microcontroller.
     */
    val messageFlow: Flow<Message>

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
    @Throws(IOException::class)
    suspend fun pullMessages()

    /**
     * Sends a [command] to the microcontroller.
     *
     * @param command The command being sent to the microcontroller.
     *
     * @throws IOException if sending the command fails.
     */
    @Throws(IOException::class)
    suspend fun sendCommand(command: Command)
}

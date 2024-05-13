package dev.slav.argb.model.messages

/**
 * Unsupported message from microcontroller.
 */
data class UnsupportedMessage(
    /**
     * Message type.
     */
    val type: String?,

    /**
     * Message arguments.
     */
    val args: List<String> = emptyList()
) : Message

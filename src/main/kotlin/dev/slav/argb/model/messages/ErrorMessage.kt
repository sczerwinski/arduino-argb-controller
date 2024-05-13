package dev.slav.argb.model.messages

/**
 * Error message from a microcontroller.
 */
data class ErrorMessage(
    /**
     * The error message.
     */
    val errorMessage: String
) : Message

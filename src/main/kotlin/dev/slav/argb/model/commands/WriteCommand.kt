package dev.slav.argb.model.commands

import dev.slav.argb.model.LEDFrame

/**
 * Write command envelope.
 */
class WriteCommand(
    /**
     * Animation frame delay.
     */
    private val frameDelay: UShort,

    /**
     * Animation frames.
     */
    private val frames: List<LEDFrame>
) : Command(TYPE_WRITE) {

    init {
        require(frames.isNotEmpty()) { "Frames cannot be empty" }
    }

    /**
     * Returns a string representation of this envelope's data.
     *
     * @return String representation of this envelope's data.
     */
    override fun dataToString(): String =
        (listOf(frames.size, frameDelay) + frames.flatMap { it.colors565 })
            .joinToString(separator = " ")
}

package dev.slav.argb.model.commands

/**
 * An envelope containing command sent to the controller.
 */
abstract class Command(
    /**
     * Integer code of the message type.
     */
    private val type: Int
) {

    /**
     * Returns a string representation of this envelope.
     *
     * @return String representation of this envelope.
     */
    override fun toString(): String =
        listOfNotNull(
            MARKER_BEGIN,
            type,
            dataToString(),
            MARKER_END
        ).joinToString(separator = " ")

    /**
     * Implement this function to return a string representation
     * of this envelope's data.
     *
     * The data should only contain integer numbers.
     *
     * If there is no data, this method should return `null`.
     *
     * @return String representation of this envelope's data or `null`.
     */
    protected abstract fun dataToString(): String?

    companion object {
        private const val MARKER_BEGIN = "BEGIN"
        private const val MARKER_END = "END"

        /**
         * Read command type code.
         */
        const val TYPE_READ = 0

        /**
         * Write command type code.
         */
        const val TYPE_WRITE = 1
    }
}
